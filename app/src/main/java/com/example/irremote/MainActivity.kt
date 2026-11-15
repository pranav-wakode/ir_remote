package com.example.irremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.irremote.ui.main.DeviceListScreen
import com.example.irremote.ui.remote.RemoteControlScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "device_list") {
                        composable("device_list") {
                            DeviceListScreen(
                                onDeviceClick = { deviceId ->
                                    navController.navigate("remote_control/$deviceId")
                                }
                            )
                        }
                        composable(
                            "remote_control/{deviceId}",
                            arguments = listOf(navArgument("deviceId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val deviceId = backStackEntry.arguments?.getInt("deviceId") ?: -1
                            RemoteControlScreen(
                                deviceId = deviceId,
                                onNavigateUp = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}