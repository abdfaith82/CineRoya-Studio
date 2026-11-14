package com.example.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.ColorGradePreset
import com.example.model.GeneratedVideo
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioElevated
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioObsidian
import com.google.firebase.Timestamp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProjectsGalleryScreen(
    isGuestMode: Boolean,
    generatedVideosState: DataUiState<List<GeneratedVideo>>,
    onOpenVideoProject: (GeneratedVideo) -> Unit,
    onDeleteVideo: (String) -> Unit,
    onBackToHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBackToHub()
    }

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioObsidian)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CinemaGold.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = CinemaGold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.projects_gallery_title),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = if (isGuestMode) {
                                    stringResource(R.string.projects_gallery_subtitle_guest)
                                } else {
                                    stringResource(R.string.projects_gallery_subtitle_cloud)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            when (generatedVideosState) {
                is DataUiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CinemaGold)
                        }
                    }
                }
                is DataUiState.Error -> {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = generatedVideosState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is DataUiState.Success -> {
                    val videos = generatedVideosState.data
                    if (videos.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StudioCard),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, StudioElevated, RoundedCornerShape(20.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MovieFilter,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.gallery_empty_title),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                    Text(
                                        text = stringResource(R.string.gallery_empty_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Button(
                                        onClick = onBackToHub,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CinemaGold,
                                            contentColor = StudioObsidian
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.nav_studio_hub))
                                    }
                                }
                            }
                        }
                    } else {
                        items(videos, key = { it.id }) { video ->
                            GeneratedVideoGalleryCard(
                                video = video,
                                onOpenProject = { onOpenVideoProject(video) },
                                onShareVideo = { file ->
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            file
                                        )
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "video/mp4"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "مشاركة الفيديو السينمائي")
                                        )
                                    } catch (_: Exception) {}
                                },
                                onDelete = { onDeleteVideo(video.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneratedVideoGalleryCard(
    video: GeneratedVideo,
    onOpenProject: () -> Unit,
    onShareVideo: (File) -> Unit,
    onDelete: () -> Unit
) {
    val thumbnailBitmap = remember(video.id, video.thumbnailBase64) {
        if (video.thumbnailBase64.isNotBlank()) {
            try {
                val bytes = Base64.decode(video.thumbnailBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    val localFile = remember(video.localFilePath) {
        if (video.localFilePath.isNotBlank()) {
            File(video.localFilePath).takeIf { it.exists() && it.length() > 0L }
        } else {
            null
        }
    }

    val formattedTimestamp = remember(video.createdAt) {
        formatArabicTimestamp(video.createdAt)
    }

    val presetLabel = remember(video.colorGradePreset) {
        ColorGradePreset.fromCode(video.colorGradePreset).labelAr
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = StudioCard),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioElevated, RoundedCornerShape(20.dp))
            .clickable { onOpenProject() }
            .testTag("gallery_video_card_${video.id}")
    ) {
        Column {
            // Thumbnail Preview Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                        )
                    )
            ) {
                if (thumbnailBitmap != null) {
                    Image(
                        bitmap = thumbnailBitmap,
                        contentDescription = video.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        CinemaGold.copy(alpha = 0.25f),
                                        StudioObsidian
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                // Play emblem overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(54.dp)
                        .clip(RoundedCornerShape(27.dp))
                        .background(StudioObsidian.copy(alpha = 0.72f))
                        .border(1.5.dp, CinemaGold, RoundedCornerShape(27.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = video.title,
                        tint = CinemaGold,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Bottom gradient scrim for thumbnail tags
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, StudioObsidian.copy(alpha = 0.88f))
                            )
                        )
                )

                // Aspect ratio & Duration badges
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = StudioObsidian.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = video.aspectRatio,
                            style = MaterialTheme.typography.labelSmall,
                            color = StudioCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = StudioObsidian.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${video.durationSeconds}ث • ${video.sceneCount} مشاهد",
                            style = MaterialTheme.typography.labelSmall,
                            color = CinemaGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (localFile != null) {
                    Surface(
                        color = StudioEmerald.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "MP4 جاهز",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StudioObsidian,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Metadata & Actions
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${video.genre} • $presetLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = StudioCyan
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_gallery_video_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.gallery_delete_video),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Creation Timestamp
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${stringResource(R.string.gallery_created_at_prefix)} $formattedTimestamp",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenProject,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_open_project_${video.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.gallery_open_project),
                            color = CinemaGold,
                            maxLines = 1
                        )
                    }

                    if (localFile != null) {
                        Button(
                            onClick = { onShareVideo(localFile) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StudioEmerald,
                                contentColor = StudioObsidian
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gallery_share_video_${video.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.gallery_share_video),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatArabicTimestamp(timestamp: Timestamp?): String {
    if (timestamp == null) return "الآن"
    return try {
        val formatter = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("ar"))
        formatter.format(timestamp.toDate())
    } catch (_: Exception) {
        "الآن"
    }
}
