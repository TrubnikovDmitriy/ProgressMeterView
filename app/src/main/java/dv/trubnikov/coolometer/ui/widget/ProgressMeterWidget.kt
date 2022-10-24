package dv.trubnikov.coolometer.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProgressMeterWidget : AppWidgetProvider() {

    @Inject
    lateinit var widgetUpdater: WidgetUpdater

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
//        widgetUpdater.updateWidgets(*appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        // TODO: Plan expedited work
//        widgetUpdater.updateWidgets(context, appWidgetId)
    }
}

