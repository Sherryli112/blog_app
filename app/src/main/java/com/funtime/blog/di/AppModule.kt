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
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:8787/"

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
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideBlogApiService(retrofit: Retrofit): BlogApiService =
        retrofit.create(BlogApiService::class.java)

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)
}
