package dv.trubnikov.coolometer.tools

fun Int.toSignString(): String {
    return if (this > 0) "+$this" else "$this"
}