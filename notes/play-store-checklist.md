# Google Play 上架檢查清單

> **兩階段目標**：先用**內部測試（Internal testing）**快速驗證 release build 能不能透過 Play Store 正常安裝、運作；接著再補齊完整資料，走**開放測試（公開測試，Open testing）**實際跑一次 Google 審核流程。內部測試、公開測試是各自獨立的軌道，不衝突，建立一個不影響之後建立另一個。
>
> 開放測試 vs 正式上架的差異：開放測試任何人可透過連結加入，但**不會出現在 Play 商店正常搜尋結果**；正式上架才會公開可搜尋。之後要轉正式版，同一個已上傳的版本可以直接升級軌道，不用重傳。

## 零、快速路徑：先做內部測試（Internal Testing）

目標：最少步驟，把 release build 透過 Play Store 正式安裝管道裝到手機上測一次。下面每項都對應到後面完整清單的某個項目，這裡只是抓出「內部測試現在就要用到」的最小集合、並排好順序。

- [x] 產生 release keystore ✅（2026-07-03 已完成，位於 `C:\Users\jiaxinli\keystores\funtime-release.jks`，忘記了一度以為沒做過）
- [x] keystore 備份到安全地方（遺失無法更新 App！）—— ⚠️ 目前只放在本機這台電腦，建議另外備份一份到雲端/其他地方
- [x] 用新 keystore 執行 `./gradlew bundleRelease`，產出正式簽名的 AAB ✅ 2026-07-07 重新驗證成功
- [x] 用 `./gradlew assembleRelease` 產出的 release APK 在模擬器測過 ✅ 2026-07-07，R8 混淆後網路連線／文章列表皆正常（200 OK 拿到真實文章 JSON，無 crash）
- [x] Play Console →「FunTime部落格」→ 測試及發布 → **內部測試** → 建立新版本，上傳 AAB ✅ 2026-07-07
- [x] 填寫版本資訊（release 標籤填「0.0.1」）✅ 2026-07-07
- [x] 新增測試人員名單 ✅ 2026-07-07（已加入公司內部人員 email）
- [x] **內部測試版本已成功發布** ✅ 2026-07-07 下午5:36（狀態：提供給內部測試人員，App bundle 版本 1 (1.0)，1.74 MB）
- [ ] 取得內部測試的**加入連結（opt-in link）**（Play Console「測試人員」分頁會有）
- [ ] 用這個連結，透過 **Play Store 正式安裝流程**（不是 adb/Android Studio 直接裝）把 App 裝到手機上
- [ ] 驗證核心功能：文章列表、文章詳情、圖片、搜尋/分類皆正常

### 第二版（0.0.2）— 圖示更新

- [x] 圖示改用官網真實 Logo（沿用 master 分支方案）+ 修正 monochrome/背景既有 bug ✅ 2026-07-13
- [x] versionCode 2 / versionName 0.0.2，`bundleRelease` 建置簽署成功 ✅ 2026-07-13
- [ ] commit 推上 `origin/pre-gamification`
- [ ] Play Console →「FunTime部落格」→ 內部測試 → 建立新版本，上傳這份 AAB
- [ ] 填寫版本資訊（release 標籤可填「0.0.2」）
- [ ] 發布後用手機（透過 Play Store 加入連結，非 adb 安裝）實機驗證圖示顯示正常，尤其留意有開「佈景主題圖示」的 Android 13+ 裝置

內部測試跑通之後，回到下面「四、隱私政策」「五、商店頁面素材」「六、內容分級 + Data safety」把還沒打勾的項目補齊，再到 Play Console 建立「**公開測試**」軌道，送同一份（或更新版）AAB 審核。

## 一、帳號

- [x] 公司已有 Google Play Console **機構帳戶**「方探科技」✅ 2026-07-07（截圖：`Downloads\BUG_IMAGE\app申請.png`）
      帳戶 ID：5688686205590403843，已通過 Android 開發人員驗證，裡面已有另一個 App「趣旅行」（`com.funtime.funtrip`，草稿/內部測試）
      不用重新申請、不用再付一次 $25
- [x] 帳戶權限已到位（`jiaxinli@funtime.com.tw` 可存取「方探科技」帳戶）✅ 2026-07-07
- [x] 已用「建立應用程式」建立 App「**FunTime部落格**」✅ 2026-07-07（套件名稱 `com.funtime.blog`）

## 二、App 簽署（Signing）

- [ ] 產生 release keystore（keytool，需自己手動執行並保管密碼，見 `docs/superpowers/plans/2026-07-03-play-store-release-readiness.md` Task 3 Step 1）
      ```
      keytool -genkey -v -keystore funtime-release.jks -alias funtime -keyalg RSA -keysize 2048 -validity 10000
      ```
- [ ] keystore 備份到安全地方（遺失無法更新 app！）
- [x] app/build.gradle.kts 加 signingConfigs（release）✅ 2026-07-03
- [x] isMinifyEnabled = true（開啟 R8 混淆）✅ 2026-07-03
- [x] 確認 proguard-rules.pro 對 Retrofit/Gson DTO 有 keep 規則 ✅ 2026-07-03

## 三、AAB

- [x] 產出 release AAB：./gradlew bundleRelease ✅ 2026-07-03（版本 1／1.0，已上傳）
- [x] 第二版：versionCode 1→2、versionName 改 "0.0.2" ✅ 2026-07-13
      輸出：app/build/outputs/bundle/release/app-release.aab（已重新建置、簽署成功）
      ⚠️ 這個 versionCode=2 會跟 master 分支目前本機的 versionCode=2（0.0.2，尚未上傳）撞號——這邊先實際上傳，master 那份如果之後也要傳，需要再把 versionCode 改成 3，避免 Play Console 拒收（同一個 App 底下 versionCode 必須跨分支嚴格遞增，不看是哪個分支）

## 四、隱私政策（開放測試必填）

- [x] 撰寫中文隱私政策草稿 ✅ 2026-07-03（見 `notes/privacy-policy-draft.md`）
- [ ] 補上聯絡 email（草稿裡的佔位符，可直接填自己的 email，內部/測試用途沒問題）
- [ ] 發布到可公開訪問的 URL
      建議放：funtime.com.tw/privacy-policy（如果暫時沒地方發布，也可以先用 GitHub Gist / Google Sites 這類免費工具生一個公開網址頂著用，開放測試審核只看網址能不能打開，不特別要求一定要放在正式網域）
- [ ] 在 Play Console 填入該 URL

## 五、商店頁面素材（開放測試必填，不能省略）

- [x] App launcher 圖示改用品牌橘色系（取代 Android 預設機器人）✅ 2026-07-03
- [x] App 圖示改用官網 favicon.png 銳化放大版本（取代先前向量重繪的 F 字形近似圖案，跟 master 分支同步）✅ 2026-07-13
      同時修正 adaptive icon 背景 pathData 缺角（原本只畫出三角形）與 monochrome 圖層沿用彩色前景（Android 13+ 佈景主題圖示會變純色色塊）這兩個既有 bug
- [ ] Play Console 用 512 × 512 px PNG（用 `funtime_website/frontend/public/logo/header_logo_c.svg` 向量重新排版輸出，畫質才夠）
- [ ] Feature Graphic：1024 × 500 px（開放測試通常必填，正式上架也要）
- [ ] 手機截圖：**至少 2 張**（開放測試門檻，正式上架建議 5–8 張）——直接用手機/模擬器對著 App 截圖即可，不用特別設計
- [ ] 應用程式名稱（最多 30 字元）
- [ ] 簡短說明（最多 80 字元）
- [ ] 完整說明（最多 4000 字元）
- [ ] 分類：新聞與雜誌 或 生活
- [ ] 聯絡電子郵件（公開顯示，可用自己的 email）

## 六、內容分級 + Data safety（開放測試必填）

- [ ] 在 Play Console 填寫內容分級問卷（約 10 分鐘）
      預期結果：普遍級
- [ ] 填寫 Data safety（資料安全）表單
      依 `notes/privacy-policy-draft.md` 的內容如實填寫：不蒐集個資、僅本機儲存書籤/搜尋歷史/離線快取、不分享第三方、無廣告追蹤 SDK
- [ ] 填寫目標受眾（Target audience）問卷

## 七、上架前程式碼確認

- [x] 將 API URL 從 10.0.2.2:8787（模擬器）改為正式後端 URL ✅ 2026-07-03
      已抽成單一常數：`data/NetworkConfig.kt`，`AppModule.kt` + 3 個 UI 檔案共用
- [x] 移除 usesCleartextTraffic（改用 HTTPS 後端後不再需要）✅ 2026-07-03
- [x] 修正 App 無法取得 Strapi 文章資料的問題 ✅ 2026-07-07
      原因：CMS 正式機 `mgmt.funtime.com.tw` 的 Cloudflare Bot Fight Mode 會擋下非瀏覽器（雲端伺服器/App）發出的請求。
      解法：`NetworkConfig.BASE_URL` 改打官網既有、不需 Token 的公開路由 `https://www.funtime.com.tw/api/proxy`；圖片改用 `NetworkConfig.IMAGE_BASE_URL = https://upd-api.funtime.com.tw`。
      已用 debug build 在模擬器實測成功（文章列表/內容/圖片皆正常）。
- [ ] **用新 keystore 重新產出的 release build 再測一次**（debug build 已驗證過，但 release 有開 R8 混淆，理論上不影響網路層，仍建議實測一次）
- [ ] android:allowBackup 決定是否保留（預設 true）
- [ ] targetSdk >= 34（目前是 36，OK）

## 八、提交審核（開放測試軌道）

- [x] Play Console 建立 App ✅ 2026-07-07（見一、帳號）
- [ ] 上傳 AAB 到 Play Console → **開放測試（Open testing）軌道**（不是正式版）
- [ ] 提交審核（開放測試審核通常比正式版快，但仍需等待）
- [ ] 審核通過後取得測試連結，實際安裝驗證一次完整流程
- [ ] （之後才做）確認開放測試穩定後，同一版本升級到正式版軌道

## 九、master 分支同步修改（0.0.2）

> 以上一到八都是 **pre-gamification** 分支的進度。master 分支另外在做登入/打卡/護照章等 gamification 功能，同一天也套用了等效的 CMS 存取修正，準備上傳成同一個「FunTime部落格」App 底下的第二個版本。**這節記錄的是 master 分支的獨立狀態**，跟上面章節是不同程式碼。

- [x] master 原本 5 個檔案各自寫死 `http://10.0.2.2:8787`（比 pre-gamification 更早期，沒有集中的 NetworkConfig），已全部改掉 ✅ 2026-07-07
      - `BlogApiService`（讀文章/地區）→ 改打 `www.funtime.com.tw/api/proxy`（不需 Token）
      - `AuthApiService`（登入/註冊/取得使用者資料）→ **直接打 `mgmt.funtime.com.tw`**——因為 `/api/proxy` 會固定用官網服務端 Token 覆蓋 Authorization 標頭，沒辦法轉發使用者自己的 JWT，`users/me` 這類需要身分的 API 會拿到錯的資料。改直接打正式機，手機真實網路 IP 預期不會被 Cloudflare Bot Fight Mode 判定為可疑（跟雲端伺服器發出的請求不同）——**這點還沒實機驗證**，待測
      - 圖片 → 改打 `upd-api.funtime.com.tw`
      - 實作方式：`AppModule.kt` 拆成兩組具名 Retrofit（`@Named("blog")` / `@Named("auth")`）
- [x] master 原本完全沒有 release 簽署設定，補上跟 pre-gamification 一致的 `signingConfigs`（沿用同一把 keystore）✅ 2026-07-07
- [x] 開啟 `isMinifyEnabled` + 補 `proguard-rules.pro` 的 Retrofit/Gson DTO keep 規則 ✅ 2026-07-07
- [x] `versionCode` 1→2、`versionName` 改 `0.0.2`（避免跟 pre-gamification 已上傳的版本 1 衝突）✅ 2026-07-07
- [x] 移除 `usesCleartextTraffic` ✅ 2026-07-07
- [x] `.gitignore` 補上 `keystore.properties` / `*.jks`（master 原本沒排除，差點把簽署密碼提交進 git）✅ 2026-07-07
- [x] `bundleRelease` / `assembleRelease` 建置成功，簽署正常 ✅ 2026-07-07
- [ ] 模擬器測試卡在開機失敗（環境問題，跟稍早黑畫面同一台裝置），尚未在裝置上實測，改用 Play Store 內部測試流程直接實機驗證
- [x] 已 commit + push 到 `origin/master`（commit `8a1e285`）✅ 2026-07-07
- [ ] 上傳這份 AAB 到 Play Console「FunTime部落格」→ 內部測試（同一個 App，第二個版本 0.0.2）
- [ ] 實機驗證：文章列表/圖片正常 + 登入/取得使用者資料（`getMe`）在直接打 `mgmt.funtime.com.tw` 的情況下不會被 Cloudflare 擋、能正確帶回使用者自己的資料

AAB 位置：`app/build/outputs/bundle/release/app-release.aab`——⚠️ 這個路徑在兩個分支底下是不同檔案，要看哪個版本得先 `git checkout` 到對應分支再確認檔案時間戳記。

---

最後更新：2026-07-13（pre-gamification 分支圖示同步更新為官網真實 Logo、修正 monochrome 與背景 pathData 既有 bug，產出 versionCode 2／0.0.2 簽署 AAB，準備上傳第二版內部測試；master 分支後續若要上傳需將 versionCode 改為 3 以上）
