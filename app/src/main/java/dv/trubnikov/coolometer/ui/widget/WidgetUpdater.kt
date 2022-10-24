package dv.trubnikov.coolometer.ui.widget

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.widget.RemoteViews
import dagger.hilt.android.qualifiers.ApplicationContext
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.domain.resositories.MessageRepository
import dv.trubnikov.coolometer.tools.getAppWidgetManager
import dv.trubnikov.coolometer.tools.getOrThrow
import dv.trubnikov.coolometer.ui.main.MainActivity
import dv.trubnikov.coolometer.ui.views.ProgressMeterDrawer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdater @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val messageRepository: MessageRepository,
) {

    private val bitmap by lazy {
        Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_HEIGHT, Bitmap.Config.ARGB_8888, true)
    }

    fun updateAllWidgets(totalProgress: Int) {
        val remoteView = createRemoteView(totalProgress)
        val widgetManager = appContext.getAppWidgetManager()
        val widgetIds = widgetManager.getAppWidgetIds(ComponentName(appContext, ProgressMeterWidget::class.java))
        for (widgetId in widgetIds) {
            widgetManager.updateAppWidget(widgetId, remoteView)
        }
    }

    fun updateWidgets(totalProgress: Int, vararg widgetIds: Int) {
        if (widgetIds.isEmpty()) return
        val remoteView = createRemoteView(totalProgress)
        val widgetManager = appContext.getAppWidgetManager()
        for (widgetId in widgetIds) {
            widgetManager.updateAppWidget(widgetId, remoteView)
        }
    }

    private fun createRemoteView(totalProgress: Int): RemoteViews {
        bitmap.eraseColor(Color.TRANSPARENT)
        val remoteView = RemoteViews(appContext.packageName, R.layout.progress_meter_widget)
        val maxProgress = 1000
        val progressMeterDrawer = ProgressMeterDrawer(appContext).apply {
            this.progress = totalProgress % maxProgress
            this.maxProgress = maxProgress
            this.totalProgress = totalProgress
        }
        progressMeterDrawer.draw(Canvas(bitmap), bitmap.width, bitmap.height)
        remoteView.setImageViewBitmap(R.id.widget_container, bitmap)
        remoteView.setOnClickPendingIntent(R.id.widget_container, createOnClickListener(appContext))
        return remoteView
    }

    private fun createOnClickListener(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        private const val WIDGET_WIDTH = 1200
        private const val WIDGET_HEIGHT = 800
    }
}