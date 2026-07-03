# Play Store 上架就緒修復 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 讓 `pre-gamification` 分支（Google Play 上架版）從「模擬器可跑」修到「可以送審、實體手機可正常使用」。

**Architecture:** 不新增功能，純修復。分三個嚴重度分層：(1) 會讓 App 在實體裝置上完全打不開的阻斷級問題、(2) Play Console 審核硬性要求、(3) 品牌/商店素材觀感問題。每一層內按檔案影響範圍由小到大排序。

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit/OkHttp, Hilt, Room, Android Gradle Plugin。

## Global Constraints

- 目標分支：`pre-gamification`（不含遊戲化功能的上架精簡版），不動 `master`。
- 正式後端網址：`https://funtime-blog-worker.funtime.workers.dev`（Cloudflare Worker，已部署）。
- 現有品牌主色已在 Compose Theme 中定義為 `BrandOrange = Color(0xFFF58900)`（`ui/theme/Color.kt`），品牌化任務一律沿用此色，不另創新色號。
- 每個 Task 完成後獨立 commit，不合併多個 Task 到同一個 commit。
- 涉及密鑰/密碼的內容（keystore 密碼）一律不進版控，透過 gitignore 的本機檔案讀取。

---

## 嚴重度 P0（阻斷級：實體手機打不開，必須先修）

### Task 1: 統一 API Base URL，改用正式 Worker 網址

**背景：** `10.0.2.2` 是 Android 模擬器對 host 機的 loopback 位址，只有模擬器連得到。這個字串目前寫死在 4 個檔案裡，實體手機裝上去會全部載入失敗。

**Files:**
- Create: `app/src/main/java/com/funtime/blog/data/NetworkConfig.kt`
- Modify: `app/src/main/java/com/funtime/blog/di/AppModule.kt:32`
- Modify: `app/src/main/java/com/funtime/blog/ui/article/ArticleDetailScreen.kt:43`
- Modify: `app/src/main/java/com/funtime/blog/ui/author/AuthorScreen.kt:24`
- Modify: `app/src/main/java/com/funtime/blog/ui/components/ArticleCard.kt:19`

**Interfaces:**
- Produces: `com.funtime.blog.data.NetworkConfig.BASE_URL: String`（不含結尾斜線，例如組圖片網址時用 `"${NetworkConfig.BASE_URL}$path"`；Retrofit baseUrl 需要結尾斜線時用 `"${NetworkConfig.BASE_URL}/"`）。

- [ ] **Step 1: 建立單一常數來源**

```kotlin
package com.funtime.blog.data

object NetworkConfig {
    const val BASE_URL = "https://funtime-blog-worker.funtime.workers.dev"
}
```

- [ ] **Step 2: 修改 AppModule.kt**

刪除第 32 行 `private const val BASE_URL = "http://10.0.2.2:8787/"`，改為：

```kotlin
import com.funtime.blog.data.NetworkConfig
```

第 76-81 行的 `provideRetrofit` 改為：

```kotlin
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("${NetworkConfig.BASE_URL}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
```

- [ ] **Step 3: 修改 ArticleDetailScreen.kt**

刪除第 43 行 `private const val STRAPI_BASE_URL = "http://10.0.2.2:8787"`，加入 import：

```kotlin
import com.funtime.blog.data.NetworkConfig
```

檔案中所有 `STRAPI_BASE_URL` 使用處改成 `NetworkConfig.BASE_URL`（`WEBSITE_BASE_URL` 那行不動，那是官網網址跟 API 無關）。

- [ ] **Step 4: 修改 AuthorScreen.kt**

刪除第 24 行 `private const val STRAPI_BASE_URL = "http://10.0.2.2:8787"`，加入 import `com.funtime.blog.data.NetworkConfig`，檔案內 `STRAPI_BASE_URL` 使用處改成 `NetworkConfig.BASE_URL`。

- [ ] **Step 5: 修改 ArticleCard.kt**

刪除第 19 行 `private const val STRAPI_BASE_URL = "http://10.0.2.2:8787"`，加入 import `com.funtime.blog.data.NetworkConfig`，第 30 行 `"$STRAPI_BASE_URL$it"` 改成 `"${NetworkConfig.BASE_URL}$it"`。

- [ ] **Step 6: 確認沒有殘留的模擬器網址**

Run: `grep -rn "10.0.2.2" app/src/main/java`
Expected: 無輸出（no matches）

- [ ] **Step 7: 編譯確認**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/funtime/blog/data/NetworkConfig.kt \
        app/src/main/java/com/funtime/blog/di/AppModule.kt \
        app/src/main/java/com/funtime/blog/ui/article/ArticleDetailScreen.kt \
        app/src/main/java/com/funtime/blog/ui/author/AuthorScreen.kt \
        app/src/main/java/com/funtime/blog/ui/components/ArticleCard.kt
git commit -m "fix: API base URL 改用正式 Worker 網址，抽成單一 NetworkConfig 常數"
```

---

### Task 2: 移除 cleartext traffic 允許

**背景：** `usesCleartextTraffic="true"` 是為了配合 Task 1 之前的 `http://10.0.2.2` 開的洞。改成 HTTPS 後這個放行沒有存在理由，留著等於平白允許明文流量，Play 審核也可能視為不必要的風險。

**Files:**
- Modify: `app/src/main/AndroidManifest.xml:17`

- [ ] **Step 1: 移除屬性**

`<application ...>` 標籤內刪除這一行：

```xml
        android:usesCleartextTraffic="true">
```

改成 `<application>` 標籤最後一個屬性 `android:theme="@style/Theme.FuntimeBlog">` 直接以 `>` 結尾。

- [ ] **Step 2: 編譯確認**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 手動驗證（實機或實體網路環境的模擬器）**

在已連上正式 Worker（Task 1 完成後）的建置版本上，打開任一篇文章、作者頁、書籤列表，確認圖片與文章內容都能正常載入（因為現在是 HTTPS，不需要 cleartext 允許）。

- [ ] **Step 4: Commit**

```bash
git add app/src/main/AndroidManifest.xml
git commit -m "fix: 移除 usesCleartextTraffic，改用正式 HTTPS 後端不再需要明文流量"
```

---

## 嚴重度 P1（Play Console 審核硬性要求）

### Task 3: Release signing config + 開啟 R8 混淆

**背景：** 目前 `isMinifyEnabled = false` 且沒有 `signingConfigs`，無法產出可上傳 Play Console 的簽署 AAB。`proguard-rules.pro` 檔案在 repo 裡實際上不存在（`build.gradle.kts` 引用了一個不存在的檔案，目前因為 `isMinifyEnabled = false` 沒被踩到）。

**Files:**
- Create: `app/proguard-rules.pro`
- Create: `keystore.properties`（repo 根目錄，**不進版控**）
- Modify: `.gitignore`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Produces: release build variant 簽署後可執行 `./gradlew bundleRelease` 產出 `app/build/outputs/bundle/release/app-release.aab`。

- [ ] **Step 1: 產生 release keystore（使用者本機手動執行，不要把密碼交給 AI 或寫進任何會進版控的檔案）**

```bash
keytool -genkey -v -keystore funtime-release.jks -alias funtime -keyalg RSA -keysize 2048 -validity 10000
```

執行時會互動詢問密碼與基本資訊（組織名稱等），照實填寫。完成後把 `funtime-release.jks` **備份到雲端硬碟或密碼管理器**（遺失就無法更新已上架的 App，Play Console 沒有救援機制）。建議放在專案外層，例如 `C:\Users\jiaxinli\keystores\funtime-release.jks`。

- [ ] **Step 2: 在 .gitignore 加入排除規則**

在 `.gitignore` 加入：

```
keystore.properties
*.jks
```

- [ ] **Step 3: 建立 keystore.properties（repo 根目錄，本機檔案，不進版控）**

```properties
storeFile=C:/Users/jiaxinli/keystores/funtime-release.jks
storePassword=<Step 1 設定的 store 密碼>
keyAlias=funtime
keyPassword=<Step 1 設定的 key 密碼>
```

- [ ] **Step 4: 建立 app/proguard-rules.pro**

```proguard
# Retrofit + Gson 用反射解析 DTO，混淆會打斷欄位對應，整包 keep
-keep class com.funtime.blog.data.api.dto.** { *; }

# Retrofit interface 本身也要 keep（動態代理）
-keep interface com.funtime.blog.data.api.** { *; }

# Room 的 @Entity / @Dao 由 KSP 產生程式碼，官方 consumer-rules 已處理，這裡不用額外加
```

- [ ] **Step 5: 修改 app/build.gradle.kts**

在檔案最上方（`plugins { ... }` 之前）加入 import（注意：不要用 `java.util.Properties()` 這種完整限定名稱直接寫在 `android {}` block 內，Kotlin DSL 在該 scope 對 `java` 有 accessor 衝突會編譯失敗，必須在檔案頂層 import 後用短名稱）：

```kotlin
import java.io.FileInputStream
import java.util.Properties
```

在 `android { ... }` block 內，`defaultConfig { ... }` 之後、`buildTypes { ... }` 之前加入：

```kotlin
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    val hasSigningConfig = keystorePropertiesFile.exists()
    if (hasSigningConfig) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    signingConfigs {
        if (hasSigningConfig) {
            create("release") {
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
```

`buildTypes { release { ... } }` 內改成：

```kotlin
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
```

> 這樣設計是讓沒有 `keystore.properties` 的環境（例如其他人 clone 這個 repo）依然能編譯 debug/release，只是 release 不會被簽署——不會因為缺密鑰檔就直接建置失敗。

- [ ] **Step 6: 確認 release build 產出成功**

Run: `./gradlew bundleRelease`
Expected: BUILD SUCCESSFUL，且產生 `app/build/outputs/bundle/release/app-release.aab`

- [ ] **Step 7: 安裝 release APK 到實機測試混淆後功能正常**

Run: `./gradlew assembleRelease` 然後手動安裝 `app/build/outputs/apk/release/app-release.apk` 到實機，走過：首頁列表 → 文章詳情（含圖片、tags）→ 搜尋 → 書籤 → 作者頁，確認沒有因為混淆造成的白屏或 crash（Gson 反射相關最容易在這裡爆掉）。

- [ ] **Step 8: Commit（只 commit 程式碼，keystore.properties 和 .jks 檔案不會被加入，因為已在 .gitignore）**

```bash
git add .gitignore app/build.gradle.kts app/proguard-rules.pro
git commit -m "build: 加入 release signing config 與開啟 R8 混淆"
```

---

### Task 4: 隱私政策（非程式碼，人工執行）

**背景：** Play Console 送審時必填隱私政策公開網址，沒有會直接卡在提交表單那一步，跟程式碼品質無關。

- [ ] **Step 1:** 撰寫中文隱私政策，至少涵蓋：蒐集哪些資料（帳號 email、閱讀紀錄——注意目前 App 有本機 Room 儲存閱讀歷史/書籤/簽到記錄，如果 pre-gamification 分支已移除簽到功能要對應調整說明範圍）、資料用途、是否第三方分享、聯絡方式。
- [ ] **Step 2:** 發布到公開可訪問的網址，建議 `https://funtime.com.tw/privacy-policy`（跟現有官網同網域，不需要另外買網域或架站）。
- [ ] **Step 3:** 在 Play Console → 應用程式內容 → 隱私政策 填入該網址。

---

## 嚴重度 P2（品牌 / 商店觀感，能送審但強烈建議先修）

### Task 5: App 圖示改用品牌識別，取代 Android 預設機器人圖示

**背景：** `ic_launcher_foreground.xml` / `ic_launcher_background.xml` 目前是 Android Studio 新建專案的預設模板圖案（機器人剪影＋裝飾線條），跟 Funtime 完全無關。`minSdk = 26` 剛好等於 Adaptive Icon 支援的最低版本，代表所有機型都會吃到 `mipmap-anydpi/ic_launcher.xml` 這組向量定義，只要換掉這兩個向量檔，實際安裝後的圖示就會全機型統一更新（不需要重繪 `mipmap-hdpi/xhdpi/...` 底下那些 `.webp` 點陣備援檔，它們在 minSdk 26 專案裡本來就不會被用到）。

**Files:**
- Modify: `app/src/main/res/drawable/ic_launcher_background.xml`
- Modify: `app/src/main/res/drawable/ic_launcher_foreground.xml`

- [ ] **Step 1: 背景改成純品牌橘色**

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#F58900"
        android:pathData="M0,0h108v108h0z" />
</vector>
```

（`#F58900` 就是 `ui/theme/Color.kt` 裡的 `BrandOrange`，維持全 App 色彩一致。）

- [ ] **Step 2: 前景改成簡單的白色 "F" 標誌，作為上架用暫時品牌圖示**

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M44,28 L74,28 L74,40 L58,40 L58,48 L66,48 L66,60 L58,60 L58,80 L44,80 Z" />
</vector>
```

> 這是暫時性的品牌化圖示（品牌色 + 簡單字母標），比預設機器人好非常多，足以上架。之後如果有正式設計師產出的 logo，直接替換這兩個 XML（或改放點陣圖也可以）即可，不影響其他程式碼。

- [ ] **Step 3: 用 Android Studio 內建的 Image Asset Studio 或直接建置後在模擬器/實機確認圖示顯示正確**

Run: `./gradlew :app:assembleDebug` 後安裝到裝置，檢查桌面圖示是否顯示為橘底白色 F，而非機器人。

- [ ] **Step 4: 清除已死的預設模板色彩（順手整理，不影響本次修復但同一次改動範圍內順手處理）**

`app/src/main/res/values/colors.xml` 內的 `purple_200/500/700`、`teal_200/700` 目前沒有任何地方引用（已用 `grep -rn "purple_\|teal_" app/src/main` 確認），可整個清空該檔案內容或刪除該檔案（先確認建置成功不會因缺檔報錯，若報錯就保留空的 `<resources></resources>`）。

- [ ] **Step 5: 編譯確認**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add app/src/main/res/drawable/ic_launcher_background.xml \
        app/src/main/res/drawable/ic_launcher_foreground.xml \
        app/src/main/res/values/colors.xml
git commit -m "fix: App 圖示改用品牌橘色系，移除預設機器人圖示與未使用的模板色彩"
```

---

### Task 6: 商店素材與文案準備（非程式碼，人工執行）

- [ ] Play Console 用的 512×512 App 圖示 PNG（可以拿 Task 5 的橘底白 F 設計直接輸出成 PNG，或等正式 logo）
- [ ] Feature Graphic 1024×500 px
- [ ] 手機截圖至少 2 張（建議 5–8 張：首頁、文章詳情、分類、搜尋、書籤）
- [ ] 應用程式名稱（≤30 字元）、簡短說明（≤80 字元）、完整說明（≤4000 字元）
- [ ] 分類選擇：新聞與雜誌 或 生活
- [ ] 公開聯絡 email
- [ ] Play Console 內容分級問卷（約 10 分鐘，預期結果：普遍級）

---

## 執行順序總結

1. Task 1 → Task 2（P0，必須完成才有可用的 App）
2. Task 3 → Task 4（P1，Play Console 提交前必須完成）
3. Task 5 → Task 6（P2，強烈建議但技術上不擋提交）

完成 Task 1+2 後，剩下的 `notes/play-store-checklist.md` 待辦清單應同步勾選對應項目。
