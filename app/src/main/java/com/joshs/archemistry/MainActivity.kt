package com.joshs.archemistry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType // Import NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joshs.archemistry.feature_ar_product.ArProductScreen // Import AR Product Screen
import com.joshs.archemistry.feature_reactant.ReactantScreen
import com.joshs.archemistry.ui.theme.ARChemistryTheme

// Define navigation routes
object AppDestinations {
    const val REACTANT_SCREEN = "reactant"
    // Removed unused destinations
    // Define argument name
    const val AR_PRODUCT_ARG_MODEL_JSON = "modelDataJson"
    // Define route with argument placeholder
    const val AR_PRODUCT_SCREEN_ROUTE = "ar_product/{$AR_PRODUCT_ARG_MODEL_JSON}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ARChemistryTheme {
                // Set up NavController
                val navController = rememberNavController()

                // Use Scaffold for basic app structure (optional, can be inside screens)
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Set up NavHost
                    NavHost(
                        navController = navController,
                        startDestination = AppDestinations.REACTANT_SCREEN, // Start at Reactant screen
                        modifier = Modifier.padding(innerPadding) // Apply padding from Scaffold
                    ) {
                        // Reactant Screen Composable
                        composable(AppDestinations.REACTANT_SCREEN) {
                            ReactantScreen(
                                // Removed unused navigation calls
                                // Construct route with encoded JSON argument
                                onNavigateToArProduct = { encodedModelDataJson ->
                                    navController.navigate("ar_product/$encodedModelDataJson")
                                }
                            )
                        }

                        // Removed unused composable routes
                        // Placeholder for AR Product Screen
                        // AR Product Screen Composable with argument
                        composable(
                            route = AppDestinations.AR_PRODUCT_SCREEN_ROUTE,
                            arguments = listOf(navArgument(AppDestinations.AR_PRODUCT_ARG_MODEL_JSON) { type = NavType.StringType })
                        ) { backStackEntry ->
                            // Extract the argument
                            val modelDataJson = backStackEntry.arguments?.getString(AppDestinations.AR_PRODUCT_ARG_MODEL_JSON)
                            // Call the actual ArProductScreen composable, passing the argument
                            ArProductScreen(modelDataJson = modelDataJson)
                        }
                    }
                }
            }
        }
    }
}

// Preview for the main navigation setup (optional)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ARChemistryTheme {
        // Previewing the whole NavHost setup is complex.
        // Preview individual screens like ReactantScreenPreview instead.
        Text("App Preview (See Screen Previews)")
    }
}