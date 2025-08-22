pipeline {
    agent any
    
    environment {
        // Docker 設定
        DOCKER_REGISTRY = 'registry.nilm.cc'
        DOCKER_BACKEND_IMAGE = 'blog-backend'
        DOCKER_FRONTEND_IMAGE = 'blog-frontend'
        
        // 代碼覆蓋率最低標準
        JACOCO_MIN_COVERAGE = '0.20'
    }
    
    tools {
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
                    docker --version
                    echo "=== 檢查 Docker 運行狀態 ==="
                    docker info
                '''
            }
        }
        
        stage('Backend Build & Test') {
            agent {
                docker {
                    image 'maven:3.9.11-eclipse-temurin-17'
                    reuseNode true
                    args '-u 103'
                }
            }
            steps {
                script {
                    try {
                        // 使用 Docker 執行後端編譯和測試
                        sh '''
                            mvn clean compile -DskipTests=true
                            # 運行測試並生成覆蓋率報告
                            mvn test jacoco:report
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "後端測試失敗: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Frontend Build') {
            agent {
                docker {
                    image 'node:22-alpine'
                    reuseNode true
                    args '-u 103'
                }
            }
            steps {
                script {
                    try {
                        // 使用 Docker 執行前端依賴安裝（修復 npm 權限問題）
                        sh '''
                            useradd 103
                            su 103
                            cd vue
                            npm ci
                            npm run build
                        '''
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "前端構建失敗: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Package Backend Application') {
            agent {
                docker {
                    image 'maven:3.8.5-openjdk-17'
                    reuseNode true
                    args '-u 103'
                }
            }
            steps {
                script {
                    try {
                        // 使用 Docker 打包 Spring Boot 應用
                        sh '''
                            mvn package -DskipTests=true -Dmaven.frontend.skip=true
                        '''
                        
                        // 驗證 JAR 檔案是否存在
                        sh 'ls -la target/*.jar'
                        
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "後端應用打包失敗: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('Build Docker Images') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                    branch 'release/*'
                }
            }
            parallel {
                stage('Build Backend Image') {
                    steps {
                        script {
                            try {
                                // 構建後端 Docker 鏡像
                                def backendImage = docker.build("${DOCKER_REGISTRY}/${DOCKER_BACKEND_IMAGE}:${BUILD_TAG}", "-f Dockerfile.backend .")
                                
                                // 也標記為 latest (如果是 main 分支)
                                if (env.BRANCH_NAME == 'main') {
                                    backendImage.tag("${DOCKER_REGISTRY}/${DOCKER_BACKEND_IMAGE}:latest")
                                }
                                
                                // 推送到私有註冊表
                                docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                                    backendImage.push()
                                    if (env.BRANCH_NAME == 'main') {
                                        backendImage.push('latest')
                                    }
                                }
                                
                            } catch (Exception e) {
                                currentBuild.result = 'FAILURE'
                                error "後端 Docker 鏡像構建失敗: ${e.getMessage()}"
                            }
                        }
                    }
                }
                
                stage('Build Frontend Image') {
                    steps {
                        script {
                            try {
                                // 構建前端 Docker 鏡像
                                def frontendImage = docker.build("${DOCKER_REGISTRY}/${DOCKER_FRONTEND_IMAGE}:${BUILD_TAG}", "-f Dockerfile.frontend .")
                                
                                // 也標記為 latest (如果是 main 分支)
                                if (env.BRANCH_NAME == 'main') {
                                    frontendImage.tag("${DOCKER_REGISTRY}/${DOCKER_FRONTEND_IMAGE}:latest")
                                }
                                
                                // 推送到私有註冊表
                                docker.withRegistry("https://${DOCKER_REGISTRY}", 'docker-registry-credentials') {
                                    frontendImage.push()
                                    if (env.BRANCH_NAME == 'main') {
                                        frontendImage.push('latest')
                                    }
                                }
                                
                            } catch (Exception e) {
                                currentBuild.result = 'FAILURE'
                                error "前端 Docker 鏡像構建失敗: ${e.getMessage()}"
                            }
                        }
                    }
                }
            }
        }
    }
    
    post {
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
                            後端鏡像: ${DOCKER_REGISTRY}/${DOCKER_BACKEND_IMAGE}:${BUILD_TAG}
                            前端鏡像: ${DOCKER_REGISTRY}/${DOCKER_FRONTEND_IMAGE}:${BUILD_TAG}
                            
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
