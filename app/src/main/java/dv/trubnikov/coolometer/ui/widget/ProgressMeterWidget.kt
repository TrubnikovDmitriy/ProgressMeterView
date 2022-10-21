package dv.trubnikov.coolometer.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import dv.trubnikov.coolometer.R
import dv.trubnikov.coolometer.tools.unsafeLazy
import dv.trubnikov.coolometer.ui.main.MainActivity
import dv.trubnikov.coolometer.ui.views.ProgressMeterDrawer

/**
 * Implementation of App Widget functionality.
 */
class ProgressMeterWidget : AppWidgetProvider() {

    companion object {
        private const val WIDGET_WIDTH = 1200
        private const val WIDGET_HEIGHT = 800

        private val bitmap by unsafeLazy {
            Bitmap.createBitmap(WIDGET_WIDTH, WIDGET_HEIGHT, Bitmap.Config.ARGB_8888, true)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        bitmap.eraseColor(Color.TRANSPARENT)
        val views = RemoteViews(context.packageName, R.layout.progress_meter_widget)
        val progressMeterDrawer = ProgressMeterDrawer(context)
        progressMeterDrawer.draw(Canvas(bitmap), bitmap.width, bitmap.height)
        views.setImageViewBitmap(R.id.widget_container, bitmap)
        views.setOnClickPendingIntent(R.id.widget_container, createOnClickListener(context))
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createOnClickListener(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}

