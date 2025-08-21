# Nginx 反向代理配置指南

本文檔說明如何使用 Nginx 反向代理來解決前後分離的 CORS 問題。

## 🏗️ 架構設計

```
用戶請求 → Nginx (80) → 前端 (3000) 或 後端 (8080)
                  ↓
            解決 CORS 問題
```

## 📁 文件結構

```
blog/
├── nginx/
│   └── nginx.conf                # 主要 nginx 配置
├── vue/
│   ├── Dockerfile.production     # 前端生產環境 Dockerfile
│   ├── nginx.conf               # 前端內部 nginx 配置
│   ├── .env.development         # 開發環境變數
│   └── .env.production          # 生產環境變數
├── docker-compose.nginx.yml     # Docker Compose 配置
└── scripts/
    └── start-nginx.sh           # 啟動腳本
```

## 🚀 快速啟動

### 方法 1: 使用啟動腳本
```bash
./scripts/start-nginx.sh
```

### 方法 2: 手動啟動
```bash
docker-compose -f docker-compose.nginx.yml up --build -d
```

## 🔧 配置說明

### Nginx 路由規則

| 路徑 | 目標 | 說明 |
|------|------|------|
| `/api/*` | 後端:8080 | 所有 API 請求 |
| `/swagger-ui/*` | 後端:8080 | API 文檔 |
| `/assets/*` | 後端:8080 | 靜態資源 |
| `/*` | 前端:3000 | 前端頁面 |

### 環境變數

#### 後端環境變數
- `SPRING_DATASOURCE_URL`: 數據庫連接
- `JWT_SECRET`: JWT 密鑰
- `GOOGLE_CLIENT_ID`: Google OAuth2 ID
- `VITE_FRONTEND_URL`: 前端地址

#### 前端環境變數
- `VITE_API_BASE_URL`: API 基礎路徑 (`/api`)
- `VITE_FRONTEND_URL`: 前端基礎地址

## 📊 服務訪問

啟動後可訪問：

- **前端**: http://localhost
- **API**: http://localhost/api
- **Swagger**: http://localhost/swagger-ui.html
- **健康檢查**: http://localhost/health

## 🛠️ 運維命令

### 查看日誌
```bash
# 查看所有服務日誌
docker-compose -f docker-compose.nginx.yml logs -f

# 查看特定服務日誌
docker-compose -f docker-compose.nginx.yml logs -f nginx
docker-compose -f docker-compose.nginx.yml logs -f backend
```

### 重啟服務
```bash
# 重啟所有服務
docker-compose -f docker-compose.nginx.yml restart

# 重啟特定服務
docker-compose -f docker-compose.nginx.yml restart nginx
```

### 停止服務
```bash
docker-compose -f docker-compose.nginx.yml down
```

### 清理資源
```bash
# 停止並刪除容器、網路
docker-compose -f docker-compose.nginx.yml down

# 同時刪除卷（注意：會清除數據庫數據）
docker-compose -f docker-compose.nginx.yml down -v
```

## 🔒 安全特性

### CORS 解決方案
- 通過 Nginx 反向代理，前後端請求同源
- 移除複雜的 CORS 配置需求
- 提升安全性

### 安全標頭
- `X-Frame-Options: SAMEORIGIN`
- `X-Content-Type-Options: nosniff`
- `X-XSS-Protection: 1; mode=block`
- `Referrer-Policy: strict-origin-when-cross-origin`
- `HSTS: max-age=31536000`

### 性能優化
- Gzip 壓縮
- 靜態資源緩存
- Keep-alive 連接
- 連接池配置

## 🐛 故障排除

### 常見問題

1. **服務啟動失敗**
   ```bash
   # 檢查端口佔用
   lsof -i :80
   
   # 查看服務狀態
   docker-compose -f docker-compose.nginx.yml ps
   ```

2. **API 請求失敗**
   ```bash
   # 檢查後端日誌
   docker-compose -f docker-compose.nginx.yml logs backend
   ```

3. **前端頁面無法加載**
   ```bash
   # 檢查前端和 nginx 日誌
   docker-compose -f docker-compose.nginx.yml logs frontend
   docker-compose -f docker-compose.nginx.yml logs nginx
   ```

### 調試模式
```bash
# 啟動時查看實時日誌
docker-compose -f docker-compose.nginx.yml up --build
```

## 📈 監控

### 健康檢查
```bash
# 檢查服務健康狀態
curl http://localhost/health

# 檢查 API 狀態
curl http://localhost/api/posts
```

### 性能監控
可以使用以下工具監控性能：
- Nginx access logs
- Docker stats
- Application metrics

這樣配置後，你的前後分離應用就完全不會有 CORS 問題了！🎉