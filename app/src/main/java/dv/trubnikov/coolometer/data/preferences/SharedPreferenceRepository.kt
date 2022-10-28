package dv.trubnikov.coolometer.data.preferences

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import dv.trubnikov.coolometer.domain.resositories.PreferenceRepository
import javax.inject.Inject

class SharedPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferenceRepository {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var bigTicks: Int
        get() = preferences.getInt(BIG_TICKS_KEY, DEFAULT_BIG_TICKS)
        set(value) = preferences.edit { putInt(BIG_TICKS_KEY, value) }

    override var smallTicks: Int
        get() = preferences.getInt(SMALL_TICKS_KEY, DEFAULT_SMALL_TICKS)
        set(value) = preferences.edit { putInt(SMALL_TICKS_KEY, value) }

    override var isWidgetOffered: Boolean
        get() = preferences.getBoolean(IS_WIDGET_OFFERED_KEY, false)
        set(value) = preferences.edit { putBoolean(IS_WIDGET_OFFERED_KEY, value) }

    override var isFirstEntrance: Boolean
        get() = preferences.getBoolean(IS_FIRST_ENTRANCE_KEY, true)
        set(value) = preferences.edit { putBoolean(IS_FIRST_ENTRANCE_KEY, value) }

    override var enableDebugButtons: Boolean
        get() = preferences.getBoolean(ENABLED_DEBUG_BUTTON_KEY, false)
        set(value) = preferences.edit { putBoolean(ENABLED_DEBUG_BUTTON_KEY, value) }

    override var isPermissionRequested: Boolean
        get() = preferences.getBoolean(IS_PERMISSION_REQUESTED_KEY, false)
        set(value) = preferences.edit { putBoolean(IS_PERMISSION_REQUESTED_KEY, value) }

    override var isDebugPanelFirstOpen: Boolean
        get() = preferences.getBoolean(IS_DEBUG_PANEL_FIRST_OPEN_KEY, true)
        set(value) = preferences.edit { putBoolean(IS_DEBUG_PANEL_FIRST_OPEN_KEY, value) }

    companion object {
        private const val PREFS_NAME = "dv.trubnikov.coolometer.data.preferences.SharedPreferences"

        private const val DEFAULT_BIG_TICKS = 5
        private const val DEFAULT_SMALL_TICKS = 2

        private const val BIG_TICKS_KEY = "BIG_TICKS_KEY"
        private const val SMALL_TICKS_KEY = "SMALL_TICKS_KEY"
        private const val IS_FIRST_ENTRANCE_KEY = "IS_FIRST_ENTRANCE_KEY"
        private const val IS_WIDGET_OFFERED_KEY = "IS_WIDGET_OFFERED_KEY"
        private const val ENABLED_DEBUG_BUTTON_KEY = "ENABLED_DEBUG_BUTTON_KEY"
        private const val IS_PERMISSION_REQUESTED_KEY = "IS_PERMISSION_REQUESTED_KEY"
        private const val IS_DEBUG_PANEL_FIRST_OPEN_KEY = "IS_DEBUG_PANEL_FIRST_OPEN_KEY"
    }
}
