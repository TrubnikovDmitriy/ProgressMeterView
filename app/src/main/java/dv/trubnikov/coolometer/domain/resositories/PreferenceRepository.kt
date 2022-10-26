package dv.trubnikov.coolometer.domain.resositories

interface PreferenceRepository {
    var bigTicks: Int
    var smallTicks: Int
    var isWidgetOffered: Boolean
    var enableDebugButtons: Boolean
}