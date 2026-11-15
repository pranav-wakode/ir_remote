package com.example.irremote.ui.remote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    var selectedButtonForOptions by remember { mutableStateOf<RemoteButton?>(null) }
    var buttonToRename by remember { mutableStateOf<RemoteButton?>(null) }

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

    selectedButtonForOptions?.let { button ->
        AlertDialog(
            onDismissRequest = { selectedButtonForOptions = null },
            title = { Text("Button Options: ${button.buttonLabel}") },
            text = { Text("What would you like to do with this button?") },
            confirmButton = {
                TextButton(onClick = {
                    buttonToRename = button
                    selectedButtonForOptions = null
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.deleteButton(button)
                    selectedButtonForOptions = null
                }) { Text("Delete", color = Color.Red) }
            }
        )
    }

    buttonToRename?.let { button ->
        var newName by remember { mutableStateOf(button.buttonLabel) }
        AlertDialog(
            onDismissRequest = { buttonToRename = null },
            title = { Text("Rename Button") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.renameButton(button, newName)
                    buttonToRename = null
                }, enabled = newName.isNotBlank()) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { buttonToRename = null }) { Text("Cancel") }
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
            val up = buttons.find { it.buttonLabel.equals("UP", ignoreCase = true) }
            val down = buttons.find { it.buttonLabel.equals("DOWN", ignoreCase = true) }
            val left = buttons.find { it.buttonLabel.equals("LEFT", ignoreCase = true) }
            val right = buttons.find { it.buttonLabel.equals("RIGHT", ignoreCase = true) }
            val ok = buttons.find { it.buttonLabel.equals("OK", ignoreCase = true) }

            if (up != null || down != null || left != null || right != null || ok != null) {
                DPadLayout(
                    up = up, down = down, left = left, right = right, ok = ok,
                    onButtonClick = { viewModel.sendButtonSignal(it) },
                    onButtonLongClick = { selectedButtonForOptions = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            val otherButtons = buttons.filter {
                !listOf("UP", "DOWN", "LEFT", "RIGHT", "OK").contains(it.buttonLabel.uppercase())
            }

            if (otherButtons.isEmpty() && buttons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No buttons yet. Tap 'Learn Code' to capture IR signals.", style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(otherButtons, key = { it.id }) { button ->
                        RemoteButtonView(
                            label = button.buttonLabel,
                            onClick = { viewModel.sendButtonSignal(button) },
                            onLongClick = { selectedButtonForOptions = button },
                            modifier = Modifier.aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RemoteButtonView(
    label: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier.then(
            if (enabled) Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
            else Modifier
        ),
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
            Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
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
    onButtonClick: (RemoteButton) -> Unit,
    onButtonLongClick: (RemoteButton) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RemoteButtonView(
            label = "UP",
            onClick = { up?.let { onButtonClick(it) } },
            onLongClick = { up?.let { onButtonLongClick(it) } },
            enabled = up != null,
            modifier = Modifier.size(width = 80.dp, height = 48.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            RemoteButtonView(
                label = "LEFT",
                onClick = { left?.let { onButtonClick(it) } },
                onLongClick = { left?.let { onButtonLongClick(it) } },
                enabled = left != null,
                modifier = Modifier.size(width = 80.dp, height = 48.dp)
            )
            RemoteButtonView(
                label = "OK",
                onClick = { ok?.let { onButtonClick(it) } },
                onLongClick = { ok?.let { onButtonLongClick(it) } },
                enabled = ok != null,
                modifier = Modifier.size(64.dp)
            )
            RemoteButtonView(
                label = "RIGHT",
                onClick = { right?.let { onButtonClick(it) } },
                onLongClick = { right?.let { onButtonLongClick(it) } },
                enabled = right != null,
                modifier = Modifier.size(width = 80.dp, height = 48.dp)
            )
        }
        RemoteButtonView(
            label = "DOWN",
            onClick = { down?.let { onButtonClick(it) } },
            onLongClick = { down?.let { onButtonLongClick(it) } },
            enabled = down != null,
            modifier = Modifier.size(width = 80.dp, height = 48.dp)
        )
    }
}