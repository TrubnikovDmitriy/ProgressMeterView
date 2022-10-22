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
