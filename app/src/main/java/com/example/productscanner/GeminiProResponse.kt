package com.example.productscanner

data class GeminiProResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata
)