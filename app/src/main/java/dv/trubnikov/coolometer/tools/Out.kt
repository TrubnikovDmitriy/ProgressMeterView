package dv.trubnikov.coolometer.tools

sealed interface Out<out T> {
    class Success<T>(val value: T) : Out<T>
    class Failure(val error: Throwable) : Out<Nothing>
}

fun <T> Out<T>.getOr(fallback: (Throwable) -> T): T {
    return when (this) {
        is Out.Success -> value
        is Out.Failure -> fallback(error)
    }
}

fun <T> Out<T>.getOrThrow(): T {
    return when (this) {
        is Out.Success -> value
        is Out.Failure -> throw error
    }
}

fun <T> Out<T>.onFailure(action: (Throwable) -> Unit) {
    if (this is Out.Failure) action(error)
}

fun <T> Out<T>.onSuccess(action: (T) -> Unit) {
    if (this is Out.Success) action(value)
}

fun <T, R> Out<T>.map(action: (T) -> R): Out<R> {
    return when (this) {
        is Out.Success -> Out.Success(action(value))
        is Out.Failure -> this
    }
}
