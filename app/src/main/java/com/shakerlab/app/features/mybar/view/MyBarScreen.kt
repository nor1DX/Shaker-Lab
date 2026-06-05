package com.shakerlab.app.features.mybar.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.shakerlab.app.ui.CocktailCard
import com.shakerlab.app.ui.Gold
import com.shakerlab.app.ui.TextGray
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBarScreen(onCocktailClick: (String) -> Unit) {
    val viewModel: MyBarViewModel = koinViewModel()

    val barIngredients by viewModel.barIngredients.observeAsState(emptyList())
    val cocktails by viewModel.cocktails.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val allIngredients by viewModel.allIngredients.observeAsState(emptyList())
    val favoriteIds by viewModel.favoriteIds.observeAsState(emptySet())

    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddIngredientDialog(
            allIngredients = allIngredients,
            currentIngredients = barIngredients,
            onAdd = { viewModel.addIngredient(it) },
            onDismiss = { showAddDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        // Header: "My Bar" + "+ Add" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Bar",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Gold),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Text("+ Add", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Ingredients chips (horizontal scroll)
        if (barIngredients.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(barIngredients) { ingredient ->
                    IngredientChip(
                        text = ingredient,
                        onRemove = { viewModel.removeIngredient(ingredient) }
                    )
                }
            }
        }

        // Divider
        HorizontalDivider(
            color = Color(0xFF2E2E2E),
            modifier = Modifier.padding(top = 4.dp)
        )


        Box(modifier = Modifier.fillMaxSize()) {
            when {
                barIngredients.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocalBar,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp).padding(bottom = 16.dp)
                        )
                        Text(
                            "Add ingredients\nto find cocktails",
                            color = TextGray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
                isLoading && cocktails.isEmpty() -> {
                    CircularProgressIndicator(
                        color = Gold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                cocktails.isEmpty() && !isLoading -> {
                    Text(
                        "No cocktails found with these ingredients",
                        color = TextGray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp)
                    )
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
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
                }
            }
        }
    }
}

@Composable
private fun IngredientChip(text: String, onRemove: () -> Unit) {
    FilterChip(
        selected = true,
        onClick = onRemove,
        label = { Text(text, fontSize = 13.sp) },
        trailingIcon = {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF252525),
            selectedLabelColor = Color.White,
            selectedTrailingIconColor = TextGray
        ),
        border = FilterChipDefaults.filterChipBorder(
            selected = true,
            enabled = true,
            selectedBorderColor = Color(0xFF3A3A3A),
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        )
    )
}

@Composable
private fun AddIngredientDialog(
    allIngredients: List<String>,
    currentIngredients: List<String>,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search, allIngredients, currentIngredients) {
        allIngredients
            .filter { it !in currentIngredients }
            .filter { search.isBlank() || it.contains(search, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1A1A),
            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Add ingredient",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Ingredient name", color = TextGray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                if (filtered.isEmpty()) {
                    Text(
                        "Nothing found",
                        color = TextGray,
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                        items(filtered) { ingredient ->
                            Text(
                                text = ingredient,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAdd(ingredient); onDismiss() }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            )
                            HorizontalDivider(color = Color(0xFF2E2E2E))
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) { Text("Cancel", color = TextGray) }
            }
        }
    }
}
