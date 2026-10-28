package ua.qwister.schoolwidget

import android.content.Context

object WidgetPrefs {
    private const val FILE = "qwister_widget_prefs"
    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)
    fun accent(c: Context) = p(c).getInt("accent", 0xFF72C7FF.toInt())
    fun dark(c: Context) = p(c).getBoolean("dark", true)
    fun showRoom(c: Context) = p(c).getBoolean("room", true)
    fun showProgress(c: Context) = p(c).getBoolean("progress", true)
    fun showCountdown(c: Context) = p(c).getBoolean("countdown", true)
    fun compact(c: Context) = p(c).getBoolean("compact", false)
    fun save(c: Context, accent: Int, dark: Boolean, room: Boolean, progress: Boolean, countdown: Boolean, compact: Boolean) {
        p(c).edit().putInt("accent", accent).putBoolean("dark", dark).putBoolean("room", room)
            .putBoolean("progress", progress).putBoolean("countdown", countdown).putBoolean("compact", compact).apply()
    }
}
