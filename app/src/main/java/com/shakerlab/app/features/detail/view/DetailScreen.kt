package com.shakerlab.app.features.detail.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shakerlab.app.domain.model.Cocktail
import com.shakerlab.app.ui.Gold
import com.shakerlab.app.ui.TextGray
import com.shakerlab.app.ui.thumbnailMedium
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreen(
    cocktailId: String,
    onBack: () -> Unit
) {
    val viewModel: DetailViewModel = koinViewModel()

    val cocktail by viewModel.cocktail.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isFavorite by viewModel.isFavorite.observeAsState(false)

    LaunchedEffect(cocktailId) {
        viewModel.loadCocktail(cocktailId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        if (isLoading && cocktail == null) {
            CircularProgressIndicator(
                color = Gold,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            cocktail?.let { c ->
                CocktailDetail(cocktail = c)
            }
        }

        // Back button (top-left, floating over content)
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(44.dp)
                .background(Color(0xCC0D0D0D), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        // Favorite button (top-right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp)
                .background(Color(0xCC0D0D0D), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = { viewModel.toggleFavorite() }) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color.Red else Color.White
                )
            }
        }

        // Random FAB (bottom-right, visible when cocktail loaded)
        if (cocktail != null) {
            FloatingActionButton(
                onClick = { viewModel.getNextRandom() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = Gold,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
        }
    }
}

@Composable
private fun CocktailDetail(cocktail: Cocktail) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        // Full-width image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(cocktail.thumbnail.thumbnailMedium())
                .crossfade(true)
                .build(),
            contentDescription = cocktail.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF1A1A1A))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .padding(bottom = 96.dp)
        ) {
            // Name
            Text(
                text = cocktail.name,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Tags
            Row(modifier = Modifier.padding(bottom = 24.dp)) {
                Tag(text = cocktail.category, isGold = true)
                Spacer(Modifier.width(8.dp))
                Tag(
                    text = if (cocktail.isAlcoholic) "Alcoholic" else "Non-alcoholic",
                    isGold = false
                )
            }

            // Ingredients
            Text(
                "Ingredients",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(vertical = 4.dp)
                    .padding(bottom = 24.dp)
            ) {
                cocktail.ingredients.forEach { ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(18.dp)
                                .background(Gold)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = ingredient.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = ingredient.measure,
                            color = Gold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Instructions
            Text(
                "Instructions",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = cocktail.instructions,
                color = TextGray,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun Tag(text: String, isGold: Boolean) {
    Box(
        modifier = Modifier
            .background(Color(0x33000000), RoundedCornerShape(4.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = if (isGold) Gold else TextGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
