package com.example.irremote.ui.remote

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.irremote.usb.IrSignal

@Composable
fun LearningModeDialog(
    learningSignal: IrSignal?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Learning Mode") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (learningSignal == null) {
                    CircularProgressIndicator()
                    Text("Point your remote at the ESP32 and press a button...")
                } else {
                    val signalType = when (learningSignal) {
                        is IrSignal.Nec -> "NEC Protocol"
                        is IrSignal.Raw -> "RAW Array"
                    }
                    Text("Signal Captured! ($signalType)", color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Button Name (e.g., Vol+, Power)") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (learningSignal != null) {
                Button(
                    onClick = { onSave(label) },
                    enabled = label.isNotBlank()
                ) {
                    Text("Save Button")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}