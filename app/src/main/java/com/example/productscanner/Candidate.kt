package com.example.productscanner

data class Candidate(
    val content: ContentX,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<SafetyRating>
)