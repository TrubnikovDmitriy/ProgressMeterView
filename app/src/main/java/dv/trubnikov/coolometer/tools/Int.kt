package dv.trubnikov.coolometer.tools

fun Int.toStringWithSign(): String {
    return if (this > 0) "+$this" else "$this"
}