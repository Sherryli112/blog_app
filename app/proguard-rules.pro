# Retrofit + Gson 用反射解析 DTO，混淆會打斷欄位對應，整包 keep
-keep class com.funtime.blog.data.api.dto.** { *; }

# Retrofit interface 本身也要 keep（動態代理）
-keep interface com.funtime.blog.data.api.** { *; }

# Room 的 @Entity / @Dao 由 KSP 產生程式碼，官方 consumer-rules 已處理，這裡不用額外加
