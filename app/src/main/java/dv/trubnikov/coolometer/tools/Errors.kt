package dv.trubnikov.coolometer.tools

import dv.trubnikov.coolometer.BuildConfig
import timber.log.Timber

fun assertFail(error: Throwable) {
    if (BuildConfig.DEBUG) {
        throw error
    } else {
        Timber.e(error)
    }
}
