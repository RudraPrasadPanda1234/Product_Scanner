package com.example.productscanner

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GeminiProService {
        @Headers("Content-Type: application/json")
        @POST("v1beta/models/gemini-1.5-flash:generateContent?key=*************************")
        fun processImage(@Body request: GeminiProRequest): Call<GeminiProResponse>
}

