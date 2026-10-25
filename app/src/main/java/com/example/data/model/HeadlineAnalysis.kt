package com.example.data.model

data class HeadlineAnalysis(
    val headline: String,
    val score: Int, // 0 to 100
    val rating: String, // "Viral Winner", "Strong Potential", "Needs Optimization"
    val wordCount: Int,
    val characterCount: Int,
    val hasNumbers: Boolean,
    val hasPowerWords: Boolean,
    val isQuestionOrHowTo: Boolean,
    val matchedPowerWords: List<String>,
    val suggestions: List<String>,
    val generatedViralVariants: List<String>
)
