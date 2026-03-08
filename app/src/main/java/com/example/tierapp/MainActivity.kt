package com.example.tierapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tierapp.auth.LoginRoute
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tierapp.core.ui.theme.TierappTheme
import com.example.tierapp.feature.family.FamilyScreen
import com.example.tierapp.feature.gallery.GalleryRoute
import com.example.tierapp.feature.pets.PetDetailRoute
import com.example.tierapp.feature.pets.PetEditRoute
import com.example.tierapp.feature.pets.PetListRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

// ---- Navigations-Routen -------------------------------------------------

@Serializable data object LoginScreenRoute
@Serializable data object TiereRoute
@Serializable data object GesundheitRoute
@Serializable data object FamilieRoute
@Serializable data object EinstellungenRoute
@Serializable data object DatenschutzRoute

@Serializable data class TierDetailRoute(val petId: String)
@Serializable data class TierBearbeitenRoute(val petId: String? = null)
@Serializable data class TierGalerieRoute(val petId: String)

// ---- Bottom-Nav ---------------------------------------------------------

private data class BottomNavItem(
    val route: Any,
    val labelRes: Int,
    val icon: ImageVector,
    val isSelected: (NavDestination?) -> Boolean,
)

private val bottomNavItems = listOf(
    BottomNavItem(
        route = TiereRoute,
        labelRes = R.string.nav_tiere,
        icon = Icons.Default.Pets,
        isSelected = { it?.hasRoute<TiereRoute>() == true },
    ),
    BottomNavItem(
        route = GesundheitRoute,
        labelRes = R.string.nav_gesundheit,
        icon = Icons.Default.HealthAndSafety,
        isSelected = { it?.hasRoute<GesundheitRoute>() == true },
    ),
    BottomNavItem(
        route = FamilieRoute,
        labelRes = R.string.nav_familie,
        icon = Icons.Default.Group,
        isSelected = { it?.hasRoute<FamilieRoute>() == true },
    ),
    BottomNavItem(
        route = EinstellungenRoute,
        labelRes = R.string.nav_einstellungen,
        icon = Icons.Default.Settings,
        isSelected = { it?.hasRoute<EinstellungenRoute>() == true },
    ),
)

private val topLevelRoutes: Set<KClass<*>> = setOf(
    TiereRoute::class,
    GesundheitRoute::class,
    FamilieRoute::class,
    EinstellungenRoute::class,
)

// ---- Activity -----------------------------------------------------------

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TierappTheme {
                TierappApp()
            }
        }
    }
}

@Composable
private fun TierappApp(
    modifier: Modifier = Modifier,
    authViewModel: com.example.tierapp.auth.LoginViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Auth-Gate: Start auf LoginScreen wenn nicht authentifiziert
    val startDestination: Any = if (authState is com.example.tierapp.auth.LoginUiState.Authenticated) {
        TiereRoute
    } else {
        LoginScreenRoute
    }

    val showBottomBar = currentDestination?.let { dest ->
        topLevelRoutes.any { dest.hasRoute(it) }
    } ?: false

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = item.isSelected(currentDestination),
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(item.labelRes),
                                )
                            },
                            label = { Text(text = stringResource(item.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<LoginScreenRoute> {
                LoginRoute(
                    onAuthenticated = {
                        navController.navigate(TiereRoute) {
                            popUpTo<LoginScreenRoute> { inclusive = true }
                        }
                    },
                    onDatenschutzClick = { navController.navigate(DatenschutzRoute) },
                )
            }
            composable<DatenschutzRoute> {
                DatenschutzScreen(onBackClick = { navController.popBackStack() })
            }
            composable<TiereRoute> {
                PetListRoute(
                    onAddPetClick = { navController.navigate(TierBearbeitenRoute()) },
                    onPetClick = { petId -> navController.navigate(TierDetailRoute(petId)) },
                )
            }
            composable<TierDetailRoute> {
                PetDetailRoute(
                    onEditClick = { petId -> navController.navigate(TierBearbeitenRoute(petId)) },
                    onGalleryClick = { petId -> navController.navigate(TierGalerieRoute(petId)) },
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable<TierGalerieRoute> {
                GalleryRoute(onBackClick = { navController.popBackStack() })
            }
            composable<TierBearbeitenRoute> {
                PetEditRoute(
                    onSaved = { navController.popBackStack() },
                    onBackClick = { navController.popBackStack() },
                )
            }
            composable<GesundheitRoute> { GesundheitPlaceholderScreen() }
            composable<FamilieRoute> {
                val currentUser =
                    (authState as? com.example.tierapp.auth.LoginUiState.Authenticated)?.user
                if (currentUser != null) {
                    FamilyScreen(currentUser = currentUser)
                }
            }
            composable<EinstellungenRoute> { EinstellungenPlaceholderScreen() }
        }
    }
}

// ---- Platzhalter-Screens ------------------------------------------------

@Composable
private fun GesundheitPlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.nav_gesundheit))
    }
}

@Composable
private fun EinstellungenPlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.nav_einstellungen))
    }
}
