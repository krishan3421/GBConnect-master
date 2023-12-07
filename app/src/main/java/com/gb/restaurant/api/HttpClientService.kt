package com.gb.restaurant.api

import okhttp3.OkHttpClient
import java.security.KeyStore
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class HttpClientService {

    companion object{
        fun getUnsafeOkHttpClient(): OkHttpClient {
            try {
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as KeyStore?)
                val trustManagers = trustManagerFactory.trustManagers
                if (trustManagers.size != 1 || trustManagers[0] !is X509TrustManager) {
                    throw IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers)) as Throwable
                }
                val trustManager = trustManagers[0] as X509TrustManager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
                val sslSocketFactory = sslContext.socketFactory

                return OkHttpClient.Builder()
                        .readTimeout(60, TimeUnit.SECONDS)
                        .connectTimeout(60, TimeUnit.SECONDS)
                         .addInterceptor(CustomInterceptor())
                        .sslSocketFactory(sslSocketFactory,trustManager)
                        .build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
    }
}