package com.example.data.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.BlogPost
import com.example.data.model.SocialPlatform
import com.example.util.TrendingTagVault
import com.example.util.UsTrafficAdvisor
import com.example.util.UtmBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

object SocialDispatcher {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    data class DispatchResult(
        val success: Boolean,
        val platform: SocialPlatform,
        val message: String,
        val destinationUrl: String,
        val estimatedReach: Int,
        val openedNativeIntent: Boolean = false
    )

    suspend fun dispatchToPlatform(
        context: Context,
        post: BlogPost,
        platform: SocialPlatform,
        webhookUrl: String? = null,
        appendUtm: Boolean = true,
        launchIntent: Boolean = false
    ): DispatchResult = withContext(Dispatchers.IO) {
        val trafficInfo = UsTrafficAdvisor.getCurrentTrafficStatus()
        val trackedUrl = if (appendUtm) {
            UtmBuilder.buildTrackedUrl(post.url, platform, "us_peak_traffic", trafficInfo.currentEstHour)
        } else {
            post.url
        }

        val platformCopy = TrendingTagVault.generatePlatformCopy(post, platform, trackedUrl)
        val baseReach = when (platform) {
            SocialPlatform.PINTEREST -> 2400
            SocialPlatform.FACEBOOK -> 1800
            SocialPlatform.TWITTER -> 3200
            SocialPlatform.QUORA -> 1500
        }
        val estimatedAudience = (baseReach * trafficInfo.estimatedReachMultiplier).toInt()

        // 1. If Webhook URL is configured, push automated webhook payload
        var webhookSuccess = false
        var webhookMessage = ""
        if (!webhookUrl.isNullOrBlank() && webhookUrl.startsWith("http")) {
            try {
                val jsonPayload = JSONObject().apply {
                    put("event", "automated_blog_post")
                    put("platform", platform.id)
                    put("title", post.title)
                    put("original_url", post.url)
                    put("tracked_url", trackedUrl)
                    put("image_url", post.imageUrl ?: "")
                    put("summary", post.summary)
                    put("formatted_post_text", platformCopy)
                    put("us_peak_status", trafficInfo.status.name)
                    put("us_est_time", trafficInfo.formattedEstTime)
                    put("estimated_reach", estimatedAudience)
                    put("timestamp", System.currentTimeMillis())
                }

                val body = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .build()

                httpClient.newCall(request).execute().use { resp ->
                    webhookSuccess = resp.isSuccessful
                    webhookMessage = if (resp.isSuccessful) {
                        "Dispatched to Webhook (${resp.code})"
                    } else {
                        "Webhook returned code: ${resp.code}"
                    }
                }
            } catch (e: Exception) {
                webhookMessage = "Webhook error: ${e.message}"
            }
        }

        // 2. Launch Native/Web Intent if requested
        var intentLaunched = false
        if (launchIntent) {
            withContext(Dispatchers.Main) {
                try {
                    val intent = buildPlatformIntent(post, platform, trackedUrl, platformCopy)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    intentLaunched = true
                } catch (e: Exception) {
                    // Fallback to standard share sheet
                    try {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, post.title)
                            putExtra(Intent.EXTRA_TEXT, platformCopy)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share to ${platform.displayName}").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        })
                        intentLaunched = true
                    } catch (_: Exception) { }
                }
            }
        }

        val success = webhookSuccess || intentLaunched || webhookUrl.isNullOrBlank()
        val finalMessage = when {
            webhookSuccess -> "Automated push delivered via Webhook to ${platform.displayName} ($webhookMessage)"
            intentLaunched -> "Launched ${platform.displayName} native poster with tracked URL"
            webhookUrl.isNullOrBlank() -> "Automated slot queued for ${platform.displayName} (${trafficInfo.formattedEstTime})"
            else -> webhookMessage
        }

        DispatchResult(
            success = success,
            platform = platform,
            message = finalMessage,
            destinationUrl = trackedUrl,
            estimatedReach = estimatedAudience,
            openedNativeIntent = intentLaunched
        )
    }

    private fun buildPlatformIntent(
        post: BlogPost,
        platform: SocialPlatform,
        trackedUrl: String,
        copy: String
    ): Intent {
        val encodedUrl = URLEncoder.encode(trackedUrl, StandardCharsets.UTF_8.toString())
        val encodedText = URLEncoder.encode(copy, StandardCharsets.UTF_8.toString())
        val encodedTitle = URLEncoder.encode(post.title, StandardCharsets.UTF_8.toString())
        val encodedMedia = post.imageUrl?.let { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) } ?: ""

        val uri = when (platform) {
            SocialPlatform.PINTEREST -> {
                // Pinterest Pin Creator URL
                Uri.parse("https://www.pinterest.com/pin/create/button/?url=$encodedUrl&media=$encodedMedia&description=$encodedTitle")
            }
            SocialPlatform.FACEBOOK -> {
                // Facebook Sharer
                Uri.parse("https://www.facebook.com/sharer/sharer.php?u=$encodedUrl")
            }
            SocialPlatform.TWITTER -> {
                // Twitter / X Intent
                Uri.parse("https://twitter.com/intent/tweet?text=$encodedText")
            }
            SocialPlatform.QUORA -> {
                // Quora Search or Share
                Uri.parse("https://www.quora.com/search?q=$encodedTitle")
            }
        }

        return Intent(Intent.ACTION_VIEW, uri)
    }
}
