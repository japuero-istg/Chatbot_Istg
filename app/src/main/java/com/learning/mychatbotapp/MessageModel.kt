package com.learning.mychatbotapp

data class MessageModel(
    val message: String,
    val role: String,
    val elapsedMs: Long = 0L,
    val startedAtMs: Long = 0L
)
