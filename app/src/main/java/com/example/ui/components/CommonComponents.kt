package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocialPlatform
import com.example.ui.theme.CoralPeak
import com.example.ui.theme.EmeraldSuccess

@Composable
fun PlatformBadge(platform: SocialPlatform, modifier: Modifier = Modifier) {
    Surface(
        color = platform.brandColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, platform.brandColor.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(text = platform.iconSymbol, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = platform.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = platform.brandColor
            )
        }
    }
}

@Composable
fun LivePulseIndicator(isPeak: Boolean, modifier: Modifier = Modifier) {
    val color = if (isPeak) CoralPeak else EmeraldSuccess
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, color.copy(alpha = 0.4f), CircleShape)
    )
}
