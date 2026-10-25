package com.example.util

import com.example.data.model.HeadlineAnalysis

object ViralHeadlineOptimizer {

    private val powerWords = listOf(
        "ultimate", "proven", "secret", "hack", "step-by-step", "blueprint", "mistakes",
        "essential", "guide", "free", "revealed", "insane", "fast", "easy", "effortless",
        "unbelievable", "strategy", "transform", "skyrocket", "guaranteed", "master",
        "powerful", "exclusive", "warning", "surprising", "genius", "top", "best"
    )

    fun analyze(headline: String): HeadlineAnalysis {
        val trimmed = headline.trim()
        if (trimmed.isEmpty()) {
            return HeadlineAnalysis(
                headline = "",
                score = 0,
                rating = "Empty",
                wordCount = 0,
                characterCount = 0,
                hasNumbers = false,
                hasPowerWords = false,
                isQuestionOrHowTo = false,
                matchedPowerWords = emptyList(),
                suggestions = listOf("Enter a headline to analyze its viral CTR score."),
                generatedViralVariants = emptyList()
            )
        }

        val words = trimmed.split(Regex("\\s+"))
        val wordCount = words.size
        val charCount = trimmed.length
        val lower = trimmed.lowercase()

        val matched = powerWords.filter { lower.contains(it) }
        val hasNumbers = Regex("\\d+").containsMatchIn(trimmed)
        val hasPowerWords = matched.isNotEmpty()
        val isQuestionOrHowTo = lower.startsWith("how to") || lower.contains("why ") || lower.contains("what is") || trimmed.endsWith("?")

        var score = 45 // baseline

        // Length optimization: 6-12 words is sweet spot for US CTR
        if (wordCount in 6..12) score += 15
        else if (wordCount in 4..14) score += 8

        // Numbers boost CTR drastically (e.g., 7 Ways, 10 Tips)
        if (hasNumbers) score += 15

        // Power words boost curiosity
        score += (matched.size * 10).coerceAtMost(25)

        // Question or How To hook
        if (isQuestionOrHowTo) score += 12

        // Cap score
        val finalScore = score.coerceIn(20, 99)

        val rating = when {
            finalScore >= 85 -> "🔥 Viral Megahit CTR"
            finalScore >= 70 -> "⚡ High Engagement Potential"
            finalScore >= 50 -> "⚠️ Moderate Reach"
            else -> "📉 Low Click-Through Rate"
        }

        val suggestions = mutableListOf<String>()
        if (!hasNumbers) {
            suggestions.add("Add a specific number (e.g., '7 Proven Steps' or '5 Mistakes to Avoid') for +15% CTR.")
        }
        if (!hasPowerWords) {
            suggestions.add("Include a US power hook word like 'Proven', 'Ultimate', 'Blueprint', or 'Secrets'.")
        }
        if (wordCount < 6) {
            suggestions.add("Headline is slightly short. Elaborate the key benefit or outcome for readers.")
        } else if (wordCount > 14) {
            suggestions.add("Headline is a bit long. Shorten it to under 70 characters so it fits on mobile feeds.")
        }
        if (!isQuestionOrHowTo && !hasNumbers) {
            suggestions.add("Frame as a 'How to...' or compelling question that US readers search on Quora/Pinterest.")
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Excellent headline! Meets top US social media CTR benchmarks.")
        }

        // Generate tailored high-converting variations
        val cleanSubject = trimmed.replace(Regex("(?i)^(how to|why|what|the)\\s+"), "").capitalizeFirst()
        val variants = listOf(
            "The Ultimate Guide to $cleanSubject (Step-by-Step Blueprint)",
            "7 Proven $cleanSubject Secrets You Wish You Knew Sooner",
            "How to Master $cleanSubject Fast (Without Common Costly Mistakes)",
            "Why Most People Fail at $cleanSubject (And What to Do Instead)"
        )

        return HeadlineAnalysis(
            headline = trimmed,
            score = finalScore,
            rating = rating,
            wordCount = wordCount,
            characterCount = charCount,
            hasNumbers = hasNumbers,
            hasPowerWords = hasPowerWords,
            isQuestionOrHowTo = isQuestionOrHowTo,
            matchedPowerWords = matched,
            suggestions = suggestions,
            generatedViralVariants = variants
        )
    }

    private fun String.capitalizeFirst(): String {
        return if (isNotEmpty()) this[0].uppercaseChar() + substring(1) else this
    }
}
