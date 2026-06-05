package ru.netology.nmedia.di

import android.content.Context
import androidx.room.Room
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.util.concurrent.TimeUnit
import kotlin.jvm.java

class DependencyContainer(
    private val context: Context
) {

    companion object{
        private const val BASE_URL: String = "${BuildConfig.BASE_URL}/api/slow/"

        @Volatile
        private var instance: DependencyContainer? = null

        fun initApp(context: Context){
            instance = DependencyContainer(context)
        }

        fun getInstance(): DependencyContainer {
            return instance!!
            }
        }

    val appAuth = AppAuth(context)

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            appAuth.authStateFlow.value.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            chain.proceed(chain.request())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val appBd = Room.databaseBuilder(context, AppDb::class.java, "app.db")
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    private val postDao = appBd.postDao

    val apiService = retrofit.create<ApiService>()

    val repository: PostRepository = PostRepositoryImpl(dao = postDao, apiService = apiService )

}