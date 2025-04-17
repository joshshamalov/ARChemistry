package com.joshs.archemistry.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joshs.archemistry.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joshs.archemistry.utils.Logger
import com.joshs.archemistry.utils.LogMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen that displays debug logs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugLogScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val logMessages = remember { Logger.getLogMessages() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Copy all logs button
                    IconButton(
                        onClick = {
                            val allLogs = logMessages.joinToString("\n") {
                                "${it.timestamp.formatTimestamp()} [${it.level}] ${it.message}"
                            }
                            copyToClipboard(context, "Debug Logs", allLogs)
                            Toast.makeText(context, "All logs copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CopyAll,
                            contentDescription = "Copy All Logs"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Log messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(logMessages) { logMessage ->
                    LogMessageItem(logMessage)
                    HorizontalDivider(color = ARButtonGray, thickness = 1.dp)
                }

                // If no logs, show a message
                if (logMessages.isEmpty()) {
                    item {
                        Text(
                            text = "No logs available",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = ARTextSecondary
                        )
                    }
                }
            }

            // Clear logs button
            Button(
                onClick = {
                    Logger.clearLogs()
                    Toast.makeText(context, "Logs cleared", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Clear Logs")
            }
        }
    }
}

@Composable
fun LogMessageItem(logMessage: LogMessage) {
    val logColor = when (logMessage.level) {
        "INFO" -> ARTextPrimary
        "ERROR" -> ARStatusRed
        "WARNING" -> ARWarningOrange
        "DEBUG" -> ARSuccessGreen
        else -> ARTextPrimary
    }

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Log content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Timestamp and level
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logMessage.timestamp.formatTimestamp(),
                    fontSize = 12.sp,
                    color = ARTextSecondary,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            color = logColor.copy(alpha = 0.2f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = logMessage.level,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = logColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Message
            Text(
                text = logMessage.message,
                fontSize = 14.sp,
                color = logColor
            )

            // Stack trace if available
            if (logMessage.stackTrace != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = logMessage.stackTrace,
                    fontSize = 12.sp,
                    color = ARTextSecondary,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 14.sp
                )
            }
        }

        // Copy button
        IconButton(
            onClick = {
                val logText = "${logMessage.timestamp.formatTimestamp()} [${logMessage.level}] ${logMessage.message}" +
                        if (logMessage.stackTrace != null) "\n${logMessage.stackTrace}" else ""
                copyToClipboard(context, "Log Entry", logText)
                Toast.makeText(context, "Log copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy Log",
                tint = ARTextSecondary
            )
        }
    }
}

private fun Long.formatTimestamp(): String {
    val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return dateFormat.format(Date(this))
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}
