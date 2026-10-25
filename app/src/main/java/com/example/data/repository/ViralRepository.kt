package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.DispatchLogEntity
import com.example.data.local.QueuePostEntity
import com.example.data.model.BlogPost
import com.example.data.model.SocialPlatform
import com.example.data.network.BloggerFeedParser
import com.example.data.network.SocialDispatcher
import com.example.util.UsTrafficAdvisor
import kotlinx.coroutines.flow.Flow

class ViralRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val queueDao = db.queueDao()
    private val logDao = db.logDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("viral_blogger_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BLOGGER_URL = "blogger_url"
        private const val KEY_AUTO_POST = "auto_post_enabled"
        private const val KEY_WEBHOOK_URL = "webhook_url"
        private const val KEY_APPEND_UTM = "append_utm_enabled"
        private const val KEY_PREFERRED_TIMEZONE = "preferred_timezone"
    }

    fun getBloggerUrl(): String = prefs.getString(KEY_BLOGGER_URL, "https://viralusblog.blogspot.com") ?: ""
    fun setBloggerUrl(url: String) = prefs.edit().putString(KEY_BLOGGER_URL, url.trim()).apply()

    fun isAutoPostEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_POST, true)
    fun setAutoPostEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_POST, enabled).apply()

    fun getWebhookUrl(): String = prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
    fun setWebhookUrl(url: String) = prefs.edit().putString(KEY_WEBHOOK_URL, url.trim()).apply()

    fun isAppendUtmEnabled(): Boolean = prefs.getBoolean(KEY_APPEND_UTM, true)
    fun setAppendUtmEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_APPEND_UTM, enabled).apply()

    fun getPreferredTimezone(): String = prefs.getString(KEY_PREFERRED_TIMEZONE, "EASTERN") ?: "EASTERN"
    fun setPreferredTimezone(zone: String) = prefs.edit().putString(KEY_PREFERRED_TIMEZONE, zone).apply()

    // Database Flows
    val queuedPosts: Flow<List<QueuePostEntity>> = queueDao.getQueuedPosts()
    val allLogs: Flow<List<DispatchLogEntity>> = logDao.getAllLogs()
    val totalSuccessCount: Flow<Int> = logDao.getSuccessfulCount()
    val totalEstimatedReach: Flow<Int?> = logDao.getTotalEstimatedReach()

    suspend fun fetchBlogPosts(customUrl: String? = null): List<BlogPost> {
        val targetUrl = customUrl ?: getBloggerUrl()
        return BloggerFeedParser.fetchBloggerPosts(targetUrl)
    }

    suspend fun scrapeSinglePost(url: String): BlogPost? {
        return BloggerFeedParser.scrapeSingleArticle(url)
    }

    suspend fun queuePostForOptimalUsPeak(post: BlogPost, platforms: List<SocialPlatform>): Long {
        val (scheduledEpoch, peakSlotName) = UsTrafficAdvisor.getNextOptimalScheduleEpochMillis()
        val platformsCsv = platforms.joinToString(",") { it.name }

        val entity = QueuePostEntity(
            title = post.title,
            originalUrl = post.url,
            summary = post.summary,
            imageUrl = post.imageUrl,
            platformsCsv = platformsCsv,
            scheduledTimeEpochMillis = scheduledEpoch,
            targetUsPeakLabel = peakSlotName,
            appendUtm = isAppendUtmEnabled(),
            status = "QUEUED"
        )
        return queueDao.insert(entity)
    }

    suspend fun cancelQueuedPost(id: Long) {
        queueDao.deleteById(id)
    }

    suspend fun dispatchImmediate(
        post: BlogPost,
        platform: SocialPlatform,
        launchIntent: Boolean = false
    ): SocialDispatcher.DispatchResult {
        val webhook = getWebhookUrl()
        val appendUtm = isAppendUtmEnabled()
        val result = SocialDispatcher.dispatchToPlatform(
            context = context,
            post = post,
            platform = platform,
            webhookUrl = webhook,
            appendUtm = appendUtm,
            launchIntent = launchIntent
        )

        // Log entry
        val trafficInfo = UsTrafficAdvisor.getCurrentTrafficStatus()
        val logEntity = DispatchLogEntity(
            postTitle = post.title,
            targetPlatform = platform.displayName,
            destinationUrl = result.destinationUrl,
            usTimeFormatted = trafficInfo.formattedEstTime,
            executionMode = if (launchIntent) "MANUAL_INTENT" else "AUTOMATED_DISPATCH",
            status = if (result.success) "SUCCESS" else "FAILED",
            message = result.message,
            estimatedReach = result.estimatedReach
        )
        logDao.insert(logEntity)

        return result
    }

    suspend fun dispatchAllPlatforms(
        post: BlogPost,
        platforms: List<SocialPlatform> = SocialPlatform.values().toList()
    ): List<SocialDispatcher.DispatchResult> {
        val results = mutableListOf<SocialDispatcher.DispatchResult>()
        for (p in platforms) {
            val res = dispatchImmediate(post, p, launchIntent = false)
            results.add(res)
        }
        return results
    }

    suspend fun processPendingQueue(): Int {
        val now = System.currentTimeMillis()
        val pending = queueDao.getPendingExecution(now)
        var dispatchedCount = 0

        for (item in pending) {
            val platformNames = item.platformsCsv.split(",")
            val platforms = platformNames.mapNotNull { name ->
                try { SocialPlatform.valueOf(name.trim()) } catch (_: Exception) { null }
            }

            val mockPost = BlogPost(
                id = "queue_${item.id}",
                title = item.title,
                url = item.originalUrl,
                summary = item.summary,
                imageUrl = item.imageUrl
            )

            for (p in platforms) {
                dispatchImmediate(mockPost, p, launchIntent = false)
            }

            queueDao.updateStatus(item.id, "POSTED")
            dispatchedCount++
        }
        return dispatchedCount
    }

    suspend fun clearLogs() {
        logDao.clearAll()
    }
}
