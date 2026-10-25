package com.example

import com.example.data.model.BlogPost
import com.example.data.model.SocialPlatform
import com.example.util.TrendingTagVault
import com.example.util.UsTrafficAdvisor
import com.example.util.UtmBuilder
import com.example.util.ViralHeadlineOptimizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TrafficAdvisorTest {

    @Test
    fun testUsTrafficStatusNotNull() {
        val status = UsTrafficAdvisor.getCurrentTrafficStatus()
        assertNotNull(status)
        assertTrue(status.currentEstHour in 0..23)
        assertTrue(status.currentEstMinute in 0..59)
        assertTrue(status.estimatedReachMultiplier > 0.5)
    }

    @Test
    fun testUtmBuilderAddsParameters() {
        val original = "https://myblog.blogspot.com/2026/10/us-traffic.html"
        val tracked = UtmBuilder.buildTrackedUrl(original, SocialPlatform.PINTEREST, "us_peak_traffic", 20)
        assertTrue(tracked.contains("utm_source=pinterest"))
        assertTrue(tracked.contains("utm_medium=automated_social"))
        assertTrue(tracked.contains("utm_campaign=us_peak_traffic"))
        assertTrue(tracked.contains("utm_content=slot_20est"))
    }

    @Test
    fun testHeadlineOptimizerScoresViralHooks() {
        val weakHeadline = "Some notes"
        val weakAnalysis = ViralHeadlineOptimizer.analyze(weakHeadline)

        val strongHeadline = "7 Proven Secrets to Explode Your US Website Traffic (Ultimate Blueprint)"
        val strongAnalysis = ViralHeadlineOptimizer.analyze(strongHeadline)

        assertTrue(strongAnalysis.score > weakAnalysis.score)
        assertTrue(strongAnalysis.hasNumbers)
        assertTrue(strongAnalysis.hasPowerWords)
        assertTrue(strongAnalysis.generatedViralVariants.isNotEmpty())
    }

    @Test
    fun testPlatformCopyGeneration() {
        val post = BlogPost(
            id = "test_1",
            title = "How to Double Blogger Earnings",
            url = "https://test.blogspot.com/post.html",
            summary = "Key actionable strategies for increasing your blog income."
        )

        val pinterestCopy = TrendingTagVault.generatePlatformCopy(post, SocialPlatform.PINTEREST, post.url)
        val twitterCopy = TrendingTagVault.generatePlatformCopy(post, SocialPlatform.TWITTER, post.url)

        assertTrue(pinterestCopy.contains(post.title))
        assertTrue(twitterCopy.length <= 280)
        assertTrue(twitterCopy.contains("https://test.blogspot.com/post.html"))
    }
}
