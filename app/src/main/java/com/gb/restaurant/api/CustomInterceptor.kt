package com.gb.restaurant.api

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class CustomInterceptor:Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
            .newBuilder()
            .build()
        println("Request URL: ${request.url().url().toString()}")
        val requestBody = request.body()
        if (requestBody != null) {
            val buffer = okio.Buffer()
            requestBody.writeTo(buffer)
            val charset = requestBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            val bodyString = buffer.readString(charset)

            println("Request Body: $bodyString")
        } else {
            println("No Request Body")
        }
        //val response =  chain.proceed(request)
        //println("url_response>>>> ${response.request().url().toString()}")
        return chain.proceed(request)
    }
}