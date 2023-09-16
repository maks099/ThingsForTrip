package com.example.thingsfortrip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.thingsfortrip.ui.theme.ThingsForTripTheme
import com.example.thingsfortrip.view_models.MainViewModel
import com.example.thingsfortrip.views.ListsView
import com.example.thingsfortrip.views.MainView
import com.example.thingsfortrip.views.ThingsList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            ThingsForTripTheme {
                NavHost(
                    navController = navController,
                    startDestination = "home"
                ){
                    composable(route = "home", content = { MainView(navController) })
                    composable(route = "things_list/{listId}",  arguments = listOf(navArgument("listId") { type = NavType.IntType })) {
                        entry ->
                        val listId: Int? = entry.arguments?.getInt("listId")
                        listId?.let { ThingsList(it) }
                    }
                    composable(route = "saved_lists", content = { ListsView(navController) })
                }
            }
        }
    }
}

