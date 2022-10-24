package dv.trubnikov.coolometer.tools

import dv.trubnikov.coolometer.tools.Out.Failure
import dv.trubnikov.coolometer.tools.Out.Success
import timber.log.Timber

sealed interface Out<out T> {
    class Success<T>(val value: T) : Out<T>
    class Failure(
        val error: Throwable,
        val message: String? = null
    ) : Out<Nothing>
}

fun <T> T.asSuccess(): Out<T> {
    return Success(this)
}

inline fun failure(errorMessage: () -> String): Failure {
    val message = errorMessage.invoke()
    val error = RuntimeException(message)
    return Failure(error, message)
}

fun failure(error: Throwable, errorMessage: (() -> String)? = null): Failure {
    return Failure(error, errorMessage?.invoke() ?: error.message)
}

inline fun <T> Out<T>.getOr(fallback: (Failure) -> T): T {
    return when (this) {
        is Success -> value
        is Failure -> fallback(this)
    }
}

fun <T> Out<T>.getOrThrow(): T {
    return when (this) {
        is Success -> value
        is Failure -> throw error
    }
}

fun <T> Out<T>.onFailure(action: (Failure) -> Unit): Out<T> {
    if (this is Failure) action(this)
    return this
}

fun <T> Out<T>.onSuccess(action: (T) -> Unit): Out<T> {
    if (this is Success) action(value)
    return this
}

fun <T, R> Out<T>.map(action: (T) -> R): Out<R> {
    return when (this) {
        is Out.Success -> Out.Success(action(value))
        is Out.Failure -> this
    }
}

fun <T, R> Out<T>.mapOut(action: (T) -> Out<R>): Out<R> {
    return when (this) {
        is Success -> action(value)
        is Failure -> this
    }
}

fun Failure.logError() {
    Timber.e(error, message)
}
