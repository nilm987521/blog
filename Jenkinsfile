pipeline {
    agent any
    
    environment {
        // Maven 設定
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        
        // Docker 設定
        DOCKER_REGISTRY = 'registry.nilm.cc'
        DOCKER_IMAGE = 'blog'
        
        // 應用設定
        SPRING_PROFILES_ACTIVE = 'prod'
        
        // 代碼覆蓋率最低標準
        JACOCO_MIN_COVERAGE = '0.20'
    }
    
    tools {
        maven 'maven-3.9'
        nodejs 'node-18'
        dockerTool 'docker'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                    env.BUILD_TAG = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}-${env.GIT_COMMIT_SHORT}"
                }
            }
        }
        
        stage('Environment Check') {
            steps {
                sh '''
                    echo "=== 環境資訊 ==="
                    java -version
                    mvn -version
                    node --version
                    npm --version
                    docker --version
                '''
            }
        }
        
        stage('Backend Build & Test') {
            steps {
                script {
                    try {
                        // 清理並編譯後端
                        sh 'mvn clean compile -DskipTests=true'
                        
                        // 運行測試並生成覆蓋率報告
                        sh 'mvn test jacoco:report'
                        
                        // 檢查代碼覆蓋率
                        sh 'mvn jacoco:check'
                        
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "後端測試失敗: ${e.getMessage()}"
                    }
                }
            }
            post {
                always {
                    // 發布測試結果
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    
                    // 發布 JaCoCo 覆蓋率報告
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                    ])
                }
            }
        }
        
        stage('Frontend Build') {
            steps {
                dir('vue') {
                    script {
                        try {
                            // 安裝前端依賴
                            sh 'npm ci'
                            
                            // 執行 ESLint 檢查
                            sh 'npm run lint'
                            
                            // 構建前端應用
                            sh 'npm run build'
                            
                        } catch (Exception e) {
                            currentBuild.result = 'FAILURE'
                            error "前端構建失敗: ${e.getMessage()}"
                        }
                    }
                }
            }
        }
        
        stage('Package Application') {
            steps {
                script {
                    try {
                        // 打包 Spring Boot 應用 (包含前端靜態資源)
                        sh 'mvn package -DskipTests=true'
                        
                        // 驗證 JAR 檔案是否存在
                        sh 'ls -la target/*.jar'
                        
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "應用打包失敗: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'release/*'
                }
            }
            steps {
                script {
                    try {
                        // 構建 Docker 鏡像
                        def dockerImage = docker.build("${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_TAG}")
                        
                        // 也標記為 latest (如果是 main 分支)
                        if (env.BRANCH_NAME == 'main') {
                            dockerImage.tag("${DOCKER_REGISTRY}/${DOCKER_IMAGE}:latest")
                        }
                        
                        // 推送到私有註冊表
                        docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                            dockerImage.push()
                            if (env.BRANCH_NAME == 'main') {
                                dockerImage.push('latest')
                            }
                        }
                        
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "Docker 鏡像構建失敗: ${e.getMessage()}"
                    }
                }
            }
        }
        
    post {
        always {
            // 清理工作空間
            cleanWs(
                cleanWhenNotBuilt: false,
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true,
                patterns: [
                    [pattern: 'target/**', type: 'EXCLUDE'],
                    [pattern: 'vue/node_modules/**', type: 'EXCLUDE']
                ]
            )
        }
        
        success {
            script {
                if (env.BRANCH_NAME == 'main') {
                    // 發送成功通知
                    emailext (
                        subject: "✅ Blog 應用部署成功 - Build #${BUILD_NUMBER}",
                        body: """
                            構建編號: ${BUILD_NUMBER}
                            分支: ${BRANCH_NAME}
                            提交: ${GIT_COMMIT_SHORT}
                            Docker 鏡像: ${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_TAG}
                            
                            構建詳情: ${BUILD_URL}
                        """,
                        to: '${DEFAULT_RECIPIENTS}'
                    )
                }
            }
        }
        
        failure {
            // 發送失敗通知
            emailext (
                subject: "❌ Blog 應用構建失敗 - Build #${BUILD_NUMBER}",
                body: """
                    構建編號: ${BUILD_NUMBER}
                    分支: ${BRANCH_NAME}
                    提交: ${GIT_COMMIT_SHORT}
                    失敗階段: ${env.STAGE_NAME}
                    
                    構建詳情: ${BUILD_URL}
                    控制台輸出: ${BUILD_URL}console
                """,
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
        
        unstable {
            // 發送不穩定構建通知
            emailext (
                subject: "⚠️ Blog 應用構建不穩定 - Build #${BUILD_NUMBER}",
                body: """
                    構建編號: ${BUILD_NUMBER}
                    分支: ${BRANCH_NAME}
                    提交: ${GIT_COMMIT_SHORT}
                    
                    可能的問題:
                    - 測試失敗但不中斷構建
                    - 代碼覆蓋率不達標
                    - SonarQube Quality Gate 警告
                    
                    構建詳情: ${BUILD_URL}
                """,
                to: '${DEFAULT_RECIPIENTS}'
            )
        }
    }
}
