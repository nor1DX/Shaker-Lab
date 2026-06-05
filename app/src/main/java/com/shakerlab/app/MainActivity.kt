package com.shakerlab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.shakerlab.app.ui.Gold
import com.shakerlab.app.ui.theme.ShakerLabTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shakerlab.app.features.catalog.view.CatalogScreen
import com.shakerlab.app.features.detail.view.DetailScreen
import com.shakerlab.app.features.favorites.view.FavoritesScreen
import com.shakerlab.app.features.mybar.view.MyBarScreen
import com.shakerlab.app.features.profile.view.ProfileScreen
import com.shakerlab.app.features.search.view.SearchScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShakerLabTheme {
                ShakerLabApp()
            }
        }
    }
}

private sealed class BottomTab(
    val route: String,
    val iconRes: Int,
    val labelRes: Int
) {
    data object Catalog : BottomTab("catalog", R.drawable.ic_catalog, R.string.nav_catalog)
    data object Search : BottomTab("search", R.drawable.ic_search, R.string.nav_search)
    data object Favorites : BottomTab("favorites", R.drawable.ic_favorites, R.string.nav_favorites)
    data object MyBar : BottomTab("mybar", R.drawable.ic_my_bar, R.string.nav_my_bar)
    data object Profile : BottomTab("profile", R.drawable.ic_profile, R.string.nav_profile)
}

private val bottomTabs = listOf(
    BottomTab.Catalog,
    BottomTab.Search,
    BottomTab.Favorites,
    BottomTab.MyBar,
    BottomTab.Profile
)

@Composable
private fun ShakerLabApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && !currentRoute.startsWith("detail/")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color(0xFF1A1A1A),
                    contentColor = Gold
                ) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = stringResource(tab.labelRes)
                                )
                            },
                            label = { Text(stringResource(tab.labelRes), fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold,
                                selectedTextColor = Gold,
                                unselectedIconColor = Color(0xFF666666),
                                unselectedTextColor = Color(0xFF666666),
                                indicatorColor = Color(0xFF252525)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "catalog",
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            composable("catalog") {
                CatalogScreen(
                    onCocktailClick = { id -> navController.navigate("detail/$id") },
                    onSettingsClick = {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("search") {
                SearchScreen(onCocktailClick = { id -> navController.navigate("detail/$id") })
            }
            composable("favorites") {
                FavoritesScreen(
                    onCocktailClick = { id -> navController.navigate("detail/$id") },
                    onGoToCatalog = {
                        navController.navigate("catalog") {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable("mybar") {
                MyBarScreen(onCocktailClick = { id -> navController.navigate("detail/$id") })
            }
            composable("profile") {
                ProfileScreen()
            }
            composable("detail/{cocktailId}") { backStackEntry ->
                val cocktailId = backStackEntry.arguments?.getString("cocktailId") ?: return@composable
                DetailScreen(
                    cocktailId = cocktailId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
