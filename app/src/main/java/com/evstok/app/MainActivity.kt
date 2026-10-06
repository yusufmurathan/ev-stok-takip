package com.evstok.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.evstok.app.ui.catalog.CatalogScreen
import com.evstok.app.ui.home.HomeScreen
import com.evstok.app.ui.shopping.ShoppingScreen
import com.evstok.app.ui.theme.EvStokTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvStokTheme {
                AppRoot()
            }
        }
    }
}

object Routes {
    const val STOCK = "stok"
    const val SHOPPING = "market"
    const val CATALOG = "katalog"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AppRoot() {
    val app = LocalContext.current.applicationContext as EvStokApp
    val navController = rememberNavController()
    val shoppingCount by app.container.repository.shoppingCount.collectAsStateWithLifecycle(initialValue = 0)
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val destinations = listOf(
        BottomDestination(Routes.STOCK, "Stok", Icons.Filled.Home),
        BottomDestination(Routes.SHOPPING, "Market", Icons.Filled.ShoppingCart),
        BottomDestination(Routes.CATALOG, "Katalog", Icons.AutoMirrored.Filled.List)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (destination.route == Routes.SHOPPING && shoppingCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError
                                            ) {
                                                Text(shoppingCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(destination.icon, contentDescription = destination.label)
                                    }
                                } else {
                                    Icon(destination.icon, contentDescription = destination.label)
                                }
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.STOCK
        ) {
            composable(Routes.STOCK) {
                HomeScreen(
                    padding = padding,
                    onNavigateToCatalog = {
                        navController.navigate(Routes.CATALOG) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Routes.SHOPPING) {
                ShoppingScreen(padding = padding)
            }
            composable(Routes.CATALOG) {
                CatalogScreen(padding = padding)
            }
        }
    }
}
