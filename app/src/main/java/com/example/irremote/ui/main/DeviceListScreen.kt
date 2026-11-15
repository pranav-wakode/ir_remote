package com.example.irremote.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.irremote.data.local.ReadymadeRemotes
import com.example.irremote.data.local.entity.RemoteDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    onDeviceClick: (Int) -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val devices by viewModel.devices.collectAsState()
    val isUsbConnected by viewModel.isUsbConnected.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var deviceToRename by remember { mutableStateOf<RemoteDevice?>(null) }

    LaunchedEffect(Unit) { viewModel.connectUsb() }

    if (showAddDialog) {
        AddDeviceDialog(
            onDismiss = { showAddDialog = false },
            onAddBlank = { name, type ->
                viewModel.addBlankDevice(name, type)
                showAddDialog = false
            },
            onAddReadymade = { template ->
                viewModel.addReadymadeDevice(template)
                showAddDialog = false
            }
        )
    }

    deviceToRename?.let { device ->
        RenameDeviceDialog(
            currentName = device.deviceName,
            onDismiss = { deviceToRename = null },
            onConfirm = { newName ->
                viewModel.renameDevice(device, newName)
                deviceToRename = null
            }
        )
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
            FloatingActionButton(onClick = { showAddDialog = true }) {
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onClick = { onDeviceClick(device.id) },
                        onRenameClick = { deviceToRename = device },
                        onDeleteClick = { viewModel.deleteDevice(device) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: RemoteDevice,
    onClick: () -> Unit,
    onRenameClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.deviceName, style = MaterialTheme.typography.titleLarge)
                Text(text = "Type: ${device.deviceType}", style = MaterialTheme.typography.bodyMedium)
            }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { expanded = false; onRenameClick() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = { expanded = false; onDeleteClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddDeviceDialog(
    onDismiss: () -> Unit,
    onAddBlank: (String, String) -> Unit,
    onAddReadymade: (ReadymadeRemotes.Template) -> Unit
) {
    var isCustom by remember { mutableStateOf(false) }
    var customName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Remote") },
        text = {
            if (!isCustom) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { isCustom = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Create Blank Remote")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Readymade Templates:", style = MaterialTheme.typography.labelLarge)
                    ReadymadeRemotes.templates.forEach { template ->
                        OutlinedButton(
                            onClick = { onAddReadymade(template) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(template.name)
                        }
                    }
                }
            } else {
                Column {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Remote Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isCustom) {
                Button(onClick = { onAddBlank(customName, "Custom") }, enabled = customName.isNotBlank()) {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (isCustom) isCustom = false else onDismiss() }) {
                Text(if (isCustom) "Back" else "Cancel")
            }
        }
    )
}

@Composable
fun RenameDeviceDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Remote") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}