package dv.trubnikov.coolometer.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dv.trubnikov.coolometer.cloud.CloudTokenProvider
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenAnalytics: CloudTokenProvider,
) : ViewModel() {

    suspend fun token(): String {
        return tokenAnalytics.getToken()
    }
}
