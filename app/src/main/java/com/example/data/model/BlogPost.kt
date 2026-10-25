package com.example.data.model

data class BlogPost(
    val id: String,
    val title: String,
    val url: String,
    val summary: String,
    val imageUrl: String? = null,
    val publishedDate: String = "",
    val categories: List<String> = emptyList(),
    val isAutoDetected: Boolean = false
)
