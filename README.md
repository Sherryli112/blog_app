# FunTime Blog Android App

FunTime 部落格的 Android 原生應用程式，以 Jetpack Compose 打造，提供文章瀏覽、搜尋、書籤收藏及遊戲化會員系統。

## 功能

- **文章瀏覽**：首頁最新 / 熱門雙分頁，支援無限捲動分頁載入
- **文章詳頁**：WebView 渲染全文、浮動目錄、相關文章、標籤跳轉、分享
- **分類瀏覽**：依主題與類別篩選文章
- **搜尋**：跨標題、摘要、標籤、內文的模糊比對
- **書籤**：收藏 / 取消收藏文章，Snackbar 即時回饋
- **作者頁**：作者簡介及其文章列表
- **遊戲化系統**（master 分支）
  - 每日簽到、連續簽到計算
  - XP 累積與等級系統（Lv1–5）
  - 個人護照：解鎖地區印章
  - 閱讀歷史記錄
  - 客製化等級頭像框
- **帳號登入 / 登出**

## 技術架構

| 分類 | 技術 |
|------|------|
| UI | Jetpack Compose + Material3 |
| 架構 | MVVM + Repository Pattern |
| 依賴注入 | Hilt 2.59.2 |
| 網路 | Retrofit 2.11.0 + OkHttp 4.12.0 |
| 本地資料庫 | Room 2.7.1 |
| 圖片載入 | Coil 2.7.0 |
| 非同步 | Kotlin Coroutines + Flow |
| 導航 | Navigation Compose 2.8.4 |
| 偏好設定 | DataStore Preferences 1.1.1 |
| 後端 | Strapi + Cloudflare Worker |

## 開發環境

- **Android Studio** Ladybug 或以上
- **JDK** 11
- **Node.js** 18 或以上（執行 Cloudflare Worker）
- **minSdk** 26（Android 8.0）
- **targetSdk** 36

## 架構說明

本專案由兩個獨立 repo 組成，開發時都需要啟動：

```
Android App (FuntimeBlog)
      ↓  HTTP 10.0.2.2:8787
Cloudflare Worker (funtime_blog_worker)  ← 本地以 wrangler dev 執行
      ↓  HTTPS + API Key
Strapi (mgmt.funtime.com.tw)  ← 遠端，無需本地啟動
```

Worker 的作用是將 Strapi API Key 注入 header，Android app 本身不持有任何 key。

## 本地開發設定

### 1. Clone 專案

```bash
git clone https://github.com/Sherryli112/blog_app.git
cd blog_app
```

### 2. 設定並啟動 Cloudflare Worker

進入 `worker` 目錄：

```bash
cd worker
npm install
```

複製範本並填入實際值（向管理員取得 API Key）：

```bash
cp .dev.vars.example .dev.vars
```

```
STRAPI_BASE_URL=https://mgmt.funtime.com.tw
STRAPI_API_KEY=<向管理員取得>
```

啟動本地 Worker（預設監聽 port 8787）：

```bash
npx wrangler dev
```

### 3. 啟動 Android App

用 Android Studio 開啟 `FuntimeBlog`，選擇模擬器後直接 Run。

模擬器以 `10.0.2.2:8787` 存取宿主機的 Worker，無需額外設定。

## 專案結構

```
app/src/main/java/com/funtime/blog/
├── data/
│   ├── api/          # Retrofit service 與 DTO
│   ├── local/        # Room database、Entity、DAO
│   └── repository/   # Repository 層
├── di/               # Hilt module
├── navigation/       # NavGraph
├── ui/
│   ├── article/      # 文章詳頁
│   ├── auth/         # 登入
│   ├── author/       # 作者頁
│   ├── bookmark/     # 書籤
│   ├── category/     # 分類
│   ├── common/       # 共用 pagination 工具
│   ├── components/   # 共用 UI 元件
│   ├── home/         # 首頁
│   ├── passport/     # 護照
│   ├── profile/      # 會員中心
│   ├── search/       # 搜尋
│   └── theme/        # Material3 主題
└── MainActivity.kt
```

## 測試

```bash
# Unit tests
./gradlew test

# Instrumented tests（需要模擬器）
./gradlew connectedAndroidTest
```
