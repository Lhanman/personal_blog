package com.personalblog.app.logging

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class AsyncLogDispatcher(
    private val config: LogConfig,
    private val formatter: LogFormatter,
    private val sinks: List<LogSink>
) {
    private sealed interface Command {
        data class Event(val event: LogEvent) : Command
        data class Flush(val ack: CompletableDeferred<Unit>) : Command
        data class Shutdown(val ack: CompletableDeferred<Unit>) : Command
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val channel = Channel<Command>(config.dispatcher.channelCapacity)
    private val worker = scope.launch {
        val buffer = mutableListOf<FormattedLogRecord>()
        while (isActive) {
            val command = withTimeoutOrNull(config.dispatcher.flushIntervalMs) { channel.receive() }
            if (command == null) {
                flushBuffer(buffer)
                continue
            }
            when (command) {
                is Command.Event -> {
                    buffer += formatter.format(command.event)
                    drainEvents(buffer)
                    if (buffer.size >= config.dispatcher.batchSize || command.event.level in config.dispatcher.flushOnLevels) {
                        flushBuffer(buffer)
                    }
                }
                is Command.Flush -> {
                    flushBuffer(buffer)
                    command.ack.complete(Unit)
                }
                is Command.Shutdown -> {
                    flushBuffer(buffer)
                    sinks.forEach { it.close() }
                    command.ack.complete(Unit)
                    break
                }
            }
        }
    }

    fun submit(event: LogEvent) {
        if (!config.enabled || !config.minLevel.allows(event.level)) return
        val result = channel.trySend(Command.Event(event))
        if (result.isFailure) handleBackpressure(event, result)
    }

    suspend fun flush() {
        val ack = CompletableDeferred<Unit>()
        channel.send(Command.Flush(ack))
        ack.await()
    }

    suspend fun close() {
        val ack = CompletableDeferred<Unit>()
        channel.send(Command.Shutdown(ack))
        ack.await()
        worker.cancel()
    }

    fun closeAsync() {
        scope.launch {
            close()
        }
    }

    private suspend fun drainEvents(buffer: MutableList<FormattedLogRecord>) {
        while (buffer.size < config.dispatcher.batchSize) {
            val next = channel.tryReceive().getOrNull() ?: break
            when (next) {
                is Command.Event -> buffer += formatter.format(next.event)
                is Command.Flush -> {
                    flushBuffer(buffer)
                    next.ack.complete(Unit)
                }
                is Command.Shutdown -> {
                    flushBuffer(buffer)
                    sinks.forEach { it.close() }
                    next.ack.complete(Unit)
                    break
                }
            }
        }
    }

    private fun handleBackpressure(event: LogEvent, result: ChannelResult<Unit>) {
        if (event.level in config.dispatcher.flushOnLevels) {
            scope.launch { channel.send(Command.Event(event)) }
            return
        }
        if (result.isClosed) {
            PlatformLogSupport.printToConsole(LogLevel.WARN, "logging dispatcher is closed; dropping log: ${event.tag}")
        }
    }

    private suspend fun flushBuffer(buffer: MutableList<FormattedLogRecord>) {
        if (buffer.isEmpty()) return
        val batch = buffer.toList()
        buffer.clear()
        sinks.forEach { sink ->
            val filtered = batch.filter { sink.minLevel.allows(it.event.level) }
            if (filtered.isNotEmpty()) {
                sink.writeBatch(filtered)
                sink.flush()
            }
        }
    }
}
