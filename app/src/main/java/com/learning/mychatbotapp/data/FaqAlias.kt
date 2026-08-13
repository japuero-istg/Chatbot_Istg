package com.learning.mychatbotapp.data

import kotlinx.serialization.Serializable

@Serializable
data class FaqAlias(
    val entryId: Int,
    val aliases: List<String>
)
