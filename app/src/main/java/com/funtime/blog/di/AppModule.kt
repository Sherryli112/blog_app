package com.funtime.blog.di

import android.content.Context
import androidx.room.Room
import com.funtime.blog.data.api.AuthApiService
import com.funtime.blog.data.api.BlogApiService
import com.funtime.blog.data.local.AppDatabase
import com.funtime.blog.data.local.BookmarkDao
import com.funtime.blog.data.local.CheckinDao
import com.funtime.blog.data.local.PassportStampDao
import com.funtime.blog.data.local.ReadingHistoryDao
import com.funtime.blog.data.local.UserSessionDataStore
import com.funtime.blog.data.local.UserStatsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // 文章/地區資料：改打官網既有、不需 Token 的公開代理路由。
    // 正式機 mgmt.funtime.com.tw 開了 Cloudflare Bot Fight Mode，會擋下雲端伺服器/App
    // 這類非瀏覽器來源的請求，改走官網 Next.js 的 /api/proxy（Server 端已內建 Strapi Token）。
    private const val BLOG_BASE_URL = "https://www.funtime.com.tw/api/proxy/"

    // 登入/使用者資料：需要原封不動轉發使用者自己的 JWT，/api/proxy 會固定用官網服務端
    // Token 覆蓋 Authorization 標頭、不能套用，改直接打正式機。手機真實網路 IP 不是雲端
    // 機房網段，不會被 Bot Fight Mode 判定為可疑流量（跟 www.funtime.com.tw/api/proxy
    // 的情況不同，那是雲端伺服器發出的請求才會被擋）。
    private const val AUTH_BASE_URL = "https://mgmt.funtime.com.tw/api/"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "bookmarks.db")
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6
            )
            .build()

    @Provides
    @Singleton
    fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideReadingHistoryDao(db: AppDatabase): ReadingHistoryDao = db.readingHistoryDao()

    @Provides
    @Singleton
    fun provideCheckinDao(db: AppDatabase): CheckinDao = db.checkinDao()

    @Provides
    @Singleton
    fun provideUserStatsDao(db: AppDatabase): UserStatsDao = db.userStatsDao()

    @Provides
    @Singleton
    fun providePassportStampDao(db: AppDatabase): PassportStampDao = db.passportStampDao()

    @Provides
    @Singleton
    fun provideUserSessionDataStore(@ApplicationContext context: Context): UserSessionDataStore =
        UserSessionDataStore(context)

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    @Named("blog")
    fun provideBlogRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BLOG_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    @Named("auth")
    fun provideAuthRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideBlogApiService(@Named("blog") retrofit: Retrofit): BlogApiService =
        retrofit.create(BlogApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(@Named("auth") retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)
}
