# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit + Gson 用反射解析 DTO，混淆會打斷欄位對應，整包 keep
-keep class com.funtime.blog.data.api.dto.** { *; }

# Retrofit interface 本身也要 keep（動態代理）
-keep interface com.funtime.blog.data.api.** { *; }

# Room 的 @Entity / @Dao 由 KSP 產生程式碼，官方 consumer-rules 已處理，這裡不用額外加