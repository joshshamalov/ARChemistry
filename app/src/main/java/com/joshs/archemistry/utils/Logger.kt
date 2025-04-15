package com.joshs.archemistry.utils

import androidx.compose.runtime.mutableStateListOf

/**
 * Simple logger for the application.
 */
object Logger {
    private val logMessages = mutableStateListOf<LogMessage>()
    
    fun log(message: String) {
        val logMessage = LogMessage(
            timestamp = System.currentTimeMillis(),
            level = "INFO",
            message = message
        )
        logMessages.add(logMessage)
        println("INFO: $message")
    }
    
    fun error(message: String, throwable: Throwable? = null) {
        val stackTrace = throwable?.stackTraceToString()
        val logMessage = LogMessage(
            timestamp = System.currentTimeMillis(),
            level = "ERROR",
            message = message,
            stackTrace = stackTrace
        )
        logMessages.add(logMessage)
        println("ERROR: $message")
        throwable?.printStackTrace()
    }
    
    fun warning(message: String) {
        val logMessage = LogMessage(
            timestamp = System.currentTimeMillis(),
            level = "WARNING",
            message = message
        )
        logMessages.add(logMessage)
        println("WARNING: $message")
    }
    
    fun debug(message: String) {
        val logMessage = LogMessage(
            timestamp = System.currentTimeMillis(),
            level = "DEBUG",
            message = message
        )
        logMessages.add(logMessage)
        println("DEBUG: $message")
    }
    
    fun getLogMessages(): List<LogMessage> {
        return logMessages
    }
    
    fun clearLogs() {
        logMessages.clear()
    }
}

/**
 * Represents a log message.
 */
data class LogMessage(
    val timestamp: Long,
    val level: String,
    val message: String,
    val stackTrace: String? = null
)
