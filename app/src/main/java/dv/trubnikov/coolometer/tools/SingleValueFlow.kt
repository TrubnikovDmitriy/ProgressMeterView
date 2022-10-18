package dv.trubnikov.coolometer.tools

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@Suppress("FunctionName")
fun <T> ReplayValueFlow(): MutableSharedFlow<T> = MutableSharedFlow(
    replay = 1, extraBufferCapacity = 0,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
)
