package com.shakerlab.app.features.search.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shakerlab.app.ui.CatalogFilterChip
import com.shakerlab.app.ui.CocktailCard
import com.shakerlab.app.ui.Gold
import com.shakerlab.app.ui.TextGray
import org.koin.androidx.compose.koinViewModel

private val searchFilters = listOf("All", "Alcoholic", "Non-alcoholic")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onCocktailClick: (String) -> Unit) {
    val viewModel: SearchViewModel = koinViewModel()
    val keyboard = LocalSoftwareKeyboardController.current

    val results by viewModel.results.observeAsState(emptyList())
    val recentSearches by viewModel.recentSearches.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val isEmpty by viewModel.isEmpty.observeAsState(false)
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())

    var query by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    fun doSearch() {
        if (query.trim().isEmpty()) return
        keyboard?.hide()
        viewModel.search(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 14.dp)
                        .size(20.dp)
                )
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    cursorBrush = SolidColor(Gold),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { doSearch() }),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 44.dp, end = 12.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.fillMaxSize()) {
                            if (query.isEmpty()) {
                                Text("Search cocktail...", color = TextGray, fontSize = 15.sp)
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Text(
                text = "Search",
                color = Gold,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .clickable { doSearch() }
                    .padding(4.dp)
            )
        }

        // Recent searches
        if (recentSearches.isNotEmpty() && results.isEmpty() && !isLoading && !isEmpty) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent searches",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Clear",
                    color = Gold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { viewModel.clearRecentSearches() }
                        .padding(4.dp)
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(recentSearches) { recent ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF252525))
                            .clickable {
                                query = recent
                                keyboard?.hide()
                                viewModel.search(recent)
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(recent, color = TextGray, fontSize = 13.sp)
                    }
                }
            }
        }

        // Filter chips (shown after search returns results)
        if (results.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                items(searchFilters) { filter ->
                    CatalogFilterChip(
                        text = filter,
                        selected = filter == selectedFilter,
                        onClick = {
                            selectedFilter = filter
                            viewModel.setAlcoholicFilter(filter)
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                isLoading -> CircularProgressIndicator(
                    color = Gold,
                    modifier = Modifier.align(Alignment.Center)
                )
                isEmpty -> Text(
                    "Nothing found",
                    color = TextGray,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                results.isNotEmpty() -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(results, key = { it.id }) { cocktail ->
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
