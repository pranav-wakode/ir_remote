package com.example.irremote.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    onDeviceClick: (Int) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val isUsbConnected by viewModel.isUsbConnected.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.connectUsb()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IR Remotes") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = if (isUsbConnected) "USB Connected" else "USB Disconnected",
                            color = if (isUsbConnected) Color.Green else Color.Red,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addPreloadedDevice() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Device")
            }
        }
    ) { padding ->
        if (devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No devices found. Click + to add.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(devices) { device ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDeviceClick(device.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = device.deviceName, style = MaterialTheme.typography.titleLarge)
                            Text(text = "Type: ${device.deviceType}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}