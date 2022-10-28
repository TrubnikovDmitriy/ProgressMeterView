package dv.trubnikov.coolometer.domain.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.messaging.FirebaseMessaging
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dv.trubnikov.coolometer.domain.cloud.CloudTokenProvider
import kotlinx.coroutines.tasks.await
import timber.log.Timber

@HiltWorker
class RetrieveTokenWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val firebaseMessaging: FirebaseMessaging,
    private val tokenProvider: CloudTokenProvider,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            val token = firebaseMessaging.token.await()
            tokenProvider.updateToken(token)
            return Result.success()
        } catch (e: Exception) {
            Timber.i(e, "Fail to retrieve token, retry later")
            return Result.retry()
        }
    }
}
