package dv.trubnikov.coolometer.ui.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenAnalytics: CloudTokenProvider,
    private val messageRepository: MessageRepository,
) : ViewModel() {

    suspend fun token(): String {
        return tokenAnalytics.getToken()
    }
}
