package dv.trubnikov.coolometer.domain.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dv.trubnikov.coolometer.domain.parsers.MessageParser
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.getOrThrow
import timber.log.Timber

@HiltWorker
class HandleMessageWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val parser: MessageParser<Data>,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            Timber.d("HandleMessageWorker - start")
            val message = parser.parse(inputData).getOrThrow()
            messageRepository.insertMessage(message).getOrThrow()
            Timber.d("HandleMessageWorker - finish")
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e,"HandleMessageWorker - error")
            return Result.retry()
        }
    }
}
