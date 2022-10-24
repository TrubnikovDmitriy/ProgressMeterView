package dv.trubnikov.coolometer.domain.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.getOrThrow
import dv.trubnikov.coolometer.ui.widget.WidgetUpdater
import timber.log.Timber

@HiltWorker
class UpdateWidgetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val messageRepository: MessageRepository,
    private val widgetUpdater: WidgetUpdater,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try {
            Timber.d("UpdateWidgetWorker - start")
            val totalScore = messageRepository.getTotalScore().getOrThrow()
            widgetUpdater.updateAllWidgets(totalScore)
            Timber.d("UpdateWidgetWorker - finish")
            return Result.success()
        } catch (e: Exception) {
            Timber.e(e,"UpdateWidgetWorker - error")
            return Result.retry()
        }
    }
}
