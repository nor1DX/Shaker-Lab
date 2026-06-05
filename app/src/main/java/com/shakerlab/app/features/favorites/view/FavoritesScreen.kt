package com.shakerlab.app.features.favorites.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakerlab.app.ui.CocktailCard
import com.shakerlab.app.ui.Gold
import com.shakerlab.app.ui.TextGray
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavoritesScreen(
    onCocktailClick: (String) -> Unit,
    onGoToCatalog: () -> Unit = {}
) {
    val viewModel: FavoritesViewModel = koinViewModel()
    val favorites by viewModel.favorites.observeAsState(emptyList())
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Text(
            text = "Favorites",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (favorites.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.size(80.dp).padding(bottom = 20.dp)
                    )
                    Text(
                        "Nothing here yet",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Add cocktails you like\nto find them here quickly",
                        color = TextGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 28.dp)
                    )
                    OutlinedButton(
                        onClick = onGoToCatalog,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                    ) {
                        Text("Go to catalog")
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favorites, key = { it.id }) { cocktail ->
                        CocktailCard(
                            cocktail = cocktail,
                            isFavorite = cocktail.id in favoriteIds,
                            onClick = { onCocktailClick(cocktail.id) },
                            onFavoriteClick = { viewModel.toggleFavorite(cocktail) }
                        )
                    }
                }
            }
        }
    }
}
