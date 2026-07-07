# Google Play 上架檢查清單

> **目標軌道：開放測試（Open testing）**——不是內部測試、也還不是正式上架。目的是實際跑一次 Google 審核流程、了解需要準備什麼。開放測試需要完整商店資訊、內容分級、Data safety 表單，跟正式上架的準備工作高度重疊，所以下面清單不能省略商店素材跟隱私政策。
>
> 開放測試 vs 正式上架的差異：開放測試任何人可透過連結加入，但**不會出現在 Play 商店正常搜尋結果**；正式上架才會公開可搜尋。之後要轉正式版，同一個已上傳的版本可以直接升級軌道，不用重傳。

## 一、帳號

- [ ] 申請 Google Play Console 帳號（play.google.com/console）
- [ ] 付 $25 美元一次性開發者費用（信用卡）
- [ ] 等待帳號審核通過（1–2 天）

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

- [x] 產出 release AAB：./gradlew bundleRelease ✅ 2026-07-03
      輸出：app/build/outputs/bundle/release/app-release.aab（已驗證簽署成功）
- [x] versionCode 從 1 開始 ✅（目前 `app/build.gradle.kts` 是 1，正式上傳前不用再改）
- [ ] versionName 決定對外版本號（目前是 "1.0"，可維持或改 "1.0.0"，開放測試階段不強制）

## 四、隱私政策（開放測試必填）

- [x] 撰寫中文隱私政策草稿 ✅ 2026-07-03（見 `notes/privacy-policy-draft.md`）
- [ ] 補上聯絡 email（草稿裡的佔位符，可直接填自己的 email，內部/測試用途沒問題）
- [ ] 發布到可公開訪問的 URL
      建議放：funtime.com.tw/privacy-policy（如果暫時沒地方發布，也可以先用 GitHub Gist / Google Sites 這類免費工具生一個公開網址頂著用，開放測試審核只看網址能不能打開，不特別要求一定要放在正式網域）
- [ ] 在 Play Console 填入該 URL

## 五、商店頁面素材（開放測試必填，不能省略）

- [x] App launcher 圖示改用品牌橘色系（取代 Android 預設機器人）✅ 2026-07-03
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

- [ ] Play Console 建立 App（名稱、預設語言、App/遊戲、免費/付費）
- [ ] 上傳 AAB 到 Play Console → **開放測試（Open testing）軌道**（不是正式版）
- [ ] 提交審核（開放測試審核通常比正式版快，但仍需等待）
- [ ] 審核通過後取得測試連結，實際安裝驗證一次完整流程
- [ ] （之後才做）確認開放測試穩定後，同一版本升級到正式版軌道

---

最後更新：2026-07-03（改為以「開放測試」為目標，重新排序優先順序）
