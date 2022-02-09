package com.au.gonannysingleactivity.webservices.api

import com.au.gonannysingleactivity.objects.Constants
import com.au.gonannysingleactivity.utils.ApplicationGlobal
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance {

    companion object {

        fun get(): Retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(getOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private fun getOkHttpClient(): OkHttpClient {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(60, TimeUnit.SECONDS)
            builder.readTimeout(60, TimeUnit.SECONDS)
            builder.writeTimeout(60, TimeUnit.SECONDS)
            builder.addNetworkInterceptor(httpLoggingInterceptor)
            builder.addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val requestBuilder = request.newBuilder()
                    .header("Authorization", ApplicationGlobal.accessToken)
                    .header("Accept", "application/json")
                val build = requestBuilder.build()
                chain.proceed(build)
            })
            return builder.build()
        }

        fun create(): ApiInterface = get().create(ApiInterface::class.java)
    }

}