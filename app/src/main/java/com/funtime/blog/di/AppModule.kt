package com.funtime.blog.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import com.funtime.blog.data.NetworkConfig
import com.funtime.blog.data.api.BlogApiService
import com.funtime.blog.data.local.BookmarkDao
import com.funtime.blog.data.local.BookmarkDatabase
import com.funtime.blog.data.local.CachedArticleDao
import com.funtime.blog.data.local.SearchHistoryDao
import com.funtime.blog.data.local.MIGRATION_1_2
import com.funtime.blog.data.local.MIGRATION_2_3
import com.google.gson.Gson
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

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("settings")
        }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideBookmarkDatabase(@ApplicationContext context: Context): BookmarkDatabase =
        Room.databaseBuilder(context, BookmarkDatabase::class.java, "bookmarks.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .build()

    @Provides
    @Singleton
    fun provideBookmarkDao(db: BookmarkDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    @Singleton
    fun provideSearchHistoryDao(db: BookmarkDatabase): SearchHistoryDao = db.searchHistoryDao()

    @Provides
    @Singleton
    fun provideCachedArticleDao(db: BookmarkDatabase): CachedArticleDao = db.cachedArticleDao()

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
            .baseUrl("${NetworkConfig.BASE_URL}/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideBlogApiService(retrofit: Retrofit): BlogApiService =
        retrofit.create(BlogApiService::class.java)
}
