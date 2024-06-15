package com.example.productscanner

data class UsageMetadata(
    val candidatesTokenCount: Int,
    val promptTokenCount: Int,
    val totalTokenCount: Int
)