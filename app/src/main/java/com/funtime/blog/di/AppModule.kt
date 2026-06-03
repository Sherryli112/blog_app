package com.funtime.blog.di

import android.content.Context
import androidx.room.Room
import com.funtime.blog.data.api.BlogApiService
import com.funtime.blog.data.local.BookmarkDao
import com.funtime.blog.data.local.BookmarkDatabase
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
    fun provideBookmarkDatabase(@ApplicationContext context: Context): BookmarkDatabase =
        Room.databaseBuilder(context, BookmarkDatabase::class.java, "bookmarks.db").build()

    @Provides
    @Singleton
    fun provideBookmarkDao(db: BookmarkDatabase): BookmarkDao = db.bookmarkDao()

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
}
