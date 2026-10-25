package com.example.util

import com.example.data.model.BlogPost
import com.example.data.model.SocialPlatform

object TrendingTagVault {

    data class NicheTagGroup(
        val category: String,
        val icon: String,
        val tags: List<String>
    )

    val niches = listOf(
        NicheTagGroup(
            category = "Tech & AI",
            icon = "💻",
            tags = listOf("#TechNews", "#AI", "#ChatGPT", "#SoftwareTools", "#CodingTips", "#FutureTech", "#ProductivityApps", "#TechTrends")
        ),
        NicheTagGroup(
            category = "Blogging & SEO",
            icon = "📈",
            tags = listOf("#BloggerLife", "#BloggingTips", "#SEOTips", "#GoogleTraffic", "#ContentCreator", "#DigitalMarketing", "#AffiliateMarketing", "#Wordpress")
        ),
        NicheTagGroup(
            category = "Finance & Money",
            icon = "💰",
            tags = listOf("#SideHustle", "#MakeMoneyOnline", "#PassiveIncome", "#PersonalFinance", "#InvestingTips", "#SaveMoney", "#FinancialFreedom", "#WealthTips")
        ),
        NicheTagGroup(
            category = "Health & Lifestyle",
            icon = "🌿",
            tags = listOf("#HealthTips", "#WellnessJourney", "#FitnessGoals", "#HealthyLiving", "#SelfCareSunday", "#MentalHealth", "#NutritionTips", "#DailyRoutine")
        ),
        NicheTagGroup(
            category = "DIY & Home",
            icon = "🏡",
            tags = listOf("#DIYProjects", "#HomeDecorIdeas", "#LifeHacks", "#BudgetFriendly", "#CraftIdeas", "#OrganizationHacks", "#ModernHome", "#EasyDIY")
        ),
        NicheTagGroup(
            category = "Travel & Adventure",
            icon = "✈️",
            tags = listOf("#TravelGuide", "#BudgetTravel", "#Wanderlust", "#HiddenGems", "#TravelHacks", "#SoloTravel", "#BucketList", "#USTravel")
        )
    )

    fun generatePlatformCopy(
        post: BlogPost,
        platform: SocialPlatform,
        trackedUrl: String,
        tags: List<String> = emptyList()
    ): String {
        val selectedTags = if (tags.isNotEmpty()) tags else listOf("#ViralBlog", "#USNews", "#Trending")
        val tagsString = selectedTags.take(4).joinToString(" ")

        return when (platform) {
            SocialPlatform.PINTEREST -> {
                "📌 ${post.title}\n\n${post.summary.take(180)}...\n\n👉 Click pin to read the full guide on our blog!\n\n$tagsString\n$trackedUrl"
            }
            SocialPlatform.FACEBOOK -> {
                "🔥 New Post: ${post.title}\n\n${post.summary.take(220)}...\n\n👇 Read the complete article here:\n$trackedUrl\n\n$tagsString"
            }
            SocialPlatform.TWITTER -> {
                val base = "${post.title.take(130)}\n\nRead more 👇\n$trackedUrl\n${selectedTags.take(2).joinToString(" ")}"
                base.take(280)
            }
            SocialPlatform.QUORA -> {
                "Question: How can you get better results with ${post.title.take(50)}?\n\nAnswer Summary:\n${post.summary.take(250)}...\n\nFor the complete in-depth step-by-step breakdown, check out our full blog post:\n$trackedUrl"
            }
        }
    }
}
