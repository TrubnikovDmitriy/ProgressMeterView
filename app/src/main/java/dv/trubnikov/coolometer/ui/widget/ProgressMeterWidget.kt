package dv.trubnikov.coolometer.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import dv.trubnikov.coolometer.domain.workers.UpdateWidgetWorker
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ProgressMeterWidget : AppWidgetProvider() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) = scheduleWork()

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) = scheduleWork()

    private fun scheduleWork() {
        val updateWidgets = OneTimeWorkRequestBuilder<UpdateWidgetWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP,
            updateWidgets
        )
        Timber.i("ProgressMeterWidget schedule expedited work for widgets updating")
    }

    companion object {
        private const val WORK_NAME = "ProgressMeterWidget_Work"
    }
}

