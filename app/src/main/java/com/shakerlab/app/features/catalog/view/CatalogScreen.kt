package com.shakerlab.app.features.catalog.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakerlab.app.ui.CatalogFilterChip
import com.shakerlab.app.ui.CocktailCard
import com.shakerlab.app.ui.Gold
import org.koin.androidx.compose.koinViewModel

@Composable
fun CatalogScreen(
    onCocktailClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val viewModel: CatalogViewModel = koinViewModel()

    val cocktails by viewModel.cocktails.observeAsState(emptyList())
    val categories by viewModel.categories.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())
    val randomId by viewModel.randomId.observeAsState(null)

    var selectedCategory by remember { mutableStateOf("") }
    val gridState = rememberLazyGridState()

    LaunchedEffect(categories) {
        if (selectedCategory.isEmpty() && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    LaunchedEffect(randomId) {
        val id = randomId
        if (id != null) {
            onCocktailClick(id)
            viewModel.onRandomNavigated()
        }
    }

    // Auto-load on scroll (last visible item within 4 of end)
    LaunchedEffect(gridState) {
        snapshotFlow {
            val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = gridState.layoutInfo.totalItemsCount
            Pair(lastVisible, total)
        }.collect { (lastVisible, total) ->
            if (total > 0 && lastVisible >= total - 4) {
                viewModel.loadMore()
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0D0D0D),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.getRandom() },
                containerColor = Gold,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Shuffle, contentDescription = null) },
                text = { Text("Random", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Custom header: "Shaker Lab" + settings icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                            append("Shaker")
                        }
                        withStyle(SpanStyle(color = Gold, fontWeight = FontWeight.Bold, fontSize = 26.sp)) {
                            append(" Lab")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = Color.White)
                }
            }

            // Category chips
            if (categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    items(categories) { category ->
                        CatalogFilterChip(
                            text = category,
                            selected = category == selectedCategory,
                            onClick = {
                                selectedCategory = category
                                viewModel.loadByCategory(category)
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading && cocktails.isEmpty()) {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(cocktails, key = { it.id }) { cocktail ->
                            CocktailCard(
                                cocktail = cocktail,
                                isFavorite = cocktail.id in favoriteIds,
                                onClick = { onCocktailClick(cocktail.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(cocktail) }
                            )
                        }
                    }
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Gold,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
