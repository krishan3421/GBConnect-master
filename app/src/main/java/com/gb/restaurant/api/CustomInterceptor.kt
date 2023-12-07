package com.gb.restaurant.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class CustomInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
            .newBuilder()
            .build()
        println("path>>>> ${request.url().url().host.toString()}")
        val response =  chain.proceed(request)
        println("url_response>>>> ${response.request().url().toString()}")
        return chain.proceed(request)
    }
}