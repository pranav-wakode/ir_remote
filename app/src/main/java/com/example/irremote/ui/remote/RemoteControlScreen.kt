package com.example.irremote.ui.remote

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.irremote.data.local.entity.RemoteButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteControlScreen(
    deviceId: Int,
    onNavigateUp: () -> Unit,
    viewModel: RemoteViewModel = hiltViewModel()
) {
    val buttons by viewModel.buttons.collectAsState()
    val learningSignal by viewModel.learningSignal.collectAsState()
    var showLearningDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deviceId) {
        viewModel.loadDevice(deviceId)
    }

    if (showLearningDialog) {
        LearningModeDialog(
            learningSignal = learningSignal,
            onDismiss = {
                viewModel.exitLearningMode()
                showLearningDialog = false
            },
            onSave = { label ->
                viewModel.saveLearnedButton(label)
                showLearningDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Remote Control") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.enterLearningMode()
                    showLearningDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Learn") },
                text = { Text("Learn Code") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // D-Pad Section
            val up = buttons.find { it.buttonLabel.equals("UP", ignoreCase = true) }
            val down = buttons.find { it.buttonLabel.equals("DOWN", ignoreCase = true) }
            val left = buttons.find { it.buttonLabel.equals("LEFT", ignoreCase = true) }
            val right = buttons.find { it.buttonLabel.equals("RIGHT", ignoreCase = true) }
            val ok = buttons.find { it.buttonLabel.equals("OK", ignoreCase = true) }

            if (up != null || down != null || left != null || right != null || ok != null) {
                DPadLayout(up, down, left, right, ok, onButtonClick = { viewModel.sendButtonSignal(it) })
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Generic Grid for remaining buttons
            val otherButtons = buttons.filter {
                !listOf("UP", "DOWN", "LEFT", "RIGHT", "OK").contains(it.buttonLabel.uppercase())
            }

            if (otherButtons.isEmpty() && buttons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No buttons yet. Tap 'Learn Code' to capture IR signals.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(otherButtons) { button ->
                        Button(
                            onClick = { viewModel.sendButtonSignal(button) },
                            modifier = Modifier.aspectRatio(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(button.buttonLabel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DPadLayout(
    up: RemoteButton?,
    down: RemoteButton?,
    left: RemoteButton?,
    right: RemoteButton?,
    ok: RemoteButton?,
    onButtonClick: (RemoteButton) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { up?.let { onButtonClick(it) } }, enabled = up != null) { Text("UP") }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(onClick = { left?.let { onButtonClick(it) } }, enabled = left != null) { Text("LEFT") }
            Button(onClick = { ok?.let { onButtonClick(it) } }, enabled = ok != null) { Text("OK") }
            Button(onClick = { right?.let { onButtonClick(it) } }, enabled = right != null) { Text("RIGHT") }
        }
        Button(onClick = { down?.let { onButtonClick(it) } }, enabled = down != null) { Text("DOWN") }
    }
}