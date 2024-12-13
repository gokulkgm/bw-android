package com.x8bit.bitwarden.data.platform.repository.model

import com.x8bit.bitwarden.data.platform.datasource.network.util.getCertPathValidatorExceptionOrNull
import com.x8bit.bitwarden.data.platform.datasource.network.util.getCertificateChainListOrEmpty
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

data class LoggableResult(
    val message: String?,
    val throwable: Throwable?,
) {
    val hasNothingToCapture: Boolean = message.isNullOrEmpty() && throwable == null
}

interface InMemoryLogManager {
    val publishedLogsFlow: Flow<String>
    fun clearLogs()
    fun registerLoggableResult(loggableResult: LoggableResult)
    fun publishLogs()
}

class InMemoryLogManagerImpl: InMemoryLogManager {
    private val loggableResults = MemoryLimitedBuffer<LoggableResult>(maxSize = 1)
    private val publishedLogsChannel: Channel<String> = Channel(Channel.BUFFERED)
    private val publishedLogsSendChannel: SendChannel<String> = publishedLogsChannel
    override val publishedLogsFlow: Flow<String> = publishedLogsChannel.consumeAsFlow()
    override fun clearLogs() {
        loggableResults.clear()
    }

    override fun registerLoggableResult(loggableResult: LoggableResult) {
        if (loggableResult.hasNothingToCapture) return
        loggableResults.add(loggableResult)
    }

    override fun publishLogs() {
        if (loggableResults.getAll().isEmpty()) return
        val publishableLogs = loggableResults.getAll().joinToString(
            separator = "\n"
        ) { result ->
            val message = "Message: ${result.message}\n".takeIf { result.message != null }.orEmpty()
            val stacktraceMessage = "Stacktrace: ${result.throwable?.stackTraceToString()}\n"
                .takeIf { result.throwable != null }.orEmpty()
            message + stacktraceMessage + result.throwable.getCertPathValidatorExceptionOrNull()
                .getCertificateChainListOrEmpty()
        }
        clearLogs()
        publishedLogsSendChannel.trySend(publishableLogs)
    }
}

class MemoryLimitedBuffer<T>(private val maxSize: Int) {

    private val buffer = ArrayDeque<T>(maxSize)

    fun add(item: T) {
        if (buffer.size >= maxSize) {
            buffer.removeFirst() // Remove oldest item if buffer is full
        }
        buffer.addLast(item) // Add new item to the end
    }

    fun getAll(): List<T> = buffer.toList()

    fun clear() {
        buffer.clear()
    }
}
