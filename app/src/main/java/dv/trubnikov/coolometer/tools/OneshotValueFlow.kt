package dv.trubnikov.coolometer.tools

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@Suppress("FunctionName")
fun <T> OneshotValueFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 0, extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)
