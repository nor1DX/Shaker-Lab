package com.shakerlab.app.ui

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

fun String.thumbnailSmall() = this
fun String.thumbnailMedium() = this

@Composable
fun CocktailImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: ThumbnailSize = ThumbnailSize.SMALL
) {
    val resolvedUrl = when (size) {
        ThumbnailSize.SMALL -> url.thumbnailSmall()
        ThumbnailSize.MEDIUM -> url.thumbnailMedium()
        ThumbnailSize.FULL -> url
    }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(resolvedUrl)
            .crossfade(300)
            .build(),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier.background(Color(0xFF252525))
    )
}

enum class ThumbnailSize { SMALL, MEDIUM, FULL }
