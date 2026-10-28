package ua.qwister.schoolwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class SchoolWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(c: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateOne(c, manager, it) }
        scheduleMinuteRefresh(c)
    }
    override fun onEnabled(c: Context) { scheduleMinuteRefresh(c) }
    override fun onDisabled(c: Context) {
        (c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(tickIntent(c))
    }

    companion object {
        private val fmt = DateTimeFormatter.ofPattern("HH:mm", Locale("uk", "UA"))
        private fun tickIntent(c: Context) = PendingIntent.getBroadcast(c, 901, Intent(c, WidgetTickReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        fun scheduleMinuteRefresh(c: Context) {
            val alarm = c.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 60_000L, 60_000L, tickIntent(c))
        }

        fun refreshAll(c: Context) {
            val manager = AppWidgetManager.getInstance(c)
            manager.getAppWidgetIds(ComponentName(c, SchoolWidgetProvider::class.java)).forEach { updateOne(c, manager, it) }
        }

        fun updateOne(c: Context, manager: AppWidgetManager, id: Int) {
            val v = RemoteViews(c.packageName, R.layout.widget_school)
            val now = LocalDateTime.now()
            val lessons = TimetableData.week[now.dayOfWeek].orEmpty()
            val t = now.toLocalTime()
            val current = lessons.firstOrNull { !t.isBefore(it.start) && t.isBefore(it.end) }
            val next = lessons.firstOrNull { t.isBefore(it.start) }
            val dark = WidgetPrefs.dark(c)
            val fg = if (dark) Color.WHITE else 0xFF202431.toInt()
            val muted = if (dark) 0xFFD4D7E2.toInt() else 0xFF555B6E.toInt()
            val accent = WidgetPrefs.accent(c)
            v.setInt(R.id.widget_root, "setBackgroundColor", if (dark) 0xFF171923.toInt() else 0xFFF3F5FA.toInt())
            v.setTextColor(R.id.widget_day, accent); v.setTextColor(R.id.widget_subject, fg)
            v.setTextColor(R.id.widget_room, muted); v.setTextColor(R.id.widget_time, fg); v.setTextColor(R.id.widget_countdown, accent)
            v.setViewVisibility(R.id.widget_room, if (WidgetPrefs.showRoom(c)) View.VISIBLE else View.GONE)
            v.setViewVisibility(R.id.widget_progress, if (WidgetPrefs.showProgress(c) && !WidgetPrefs.compact(c)) View.VISIBLE else View.GONE)
            v.setViewVisibility(R.id.widget_countdown, if (WidgetPrefs.showCountdown(c)) View.VISIBLE else View.GONE)
            v.setTextViewText(R.id.widget_day, TimetableData.dayNames[now.dayOfWeek] ?: "РОЗКЛАД")
            when {
                current != null -> {
                    val total = Duration.between(current.start, current.end).seconds.coerceAtLeast(1)
                    val elapsed = Duration.between(current.start, t).seconds.coerceIn(0, total)
                    val pct = (elapsed * 100 / total).toInt()
                    val left = Duration.between(t, current.end).toMinutes().coerceAtLeast(0)
                    v.setTextViewText(R.id.widget_subject, current.subject)
                    v.setTextViewText(R.id.widget_room, if (current.room.isBlank()) "Кабінет не вказано" else "Кабінет ${current.room}")
                    v.setProgressBar(R.id.widget_progress, 100, pct, false)
                    v.setProgressBarTintList(R.id.widget_progress, android.content.res.ColorStateList.valueOf(accent))
                    v.setTextViewText(R.id.widget_time, "${current.start.format(fmt)}–${current.end.format(fmt)}")
                    v.setTextViewText(R.id.widget_countdown, "До дзвінка: ${left} хв • $pct%")
                }
                next != null -> {
                    val mins = Duration.between(t, next.start).toMinutes().coerceAtLeast(0)
                    v.setTextViewText(R.id.widget_subject, "Далі: ${next.subject}")
                    v.setTextViewText(R.id.widget_room, if (next.room.isBlank()) "Кабінет не вказано" else "Кабінет ${next.room}")
                    v.setProgressBar(R.id.widget_progress, 100, 0, false)
                    v.setTextViewText(R.id.widget_time, "${next.start.format(fmt)}–${next.end.format(fmt)}")
                    v.setTextViewText(R.id.widget_countdown, "До початку: ${mins} хв")
                }
                else -> {
                    v.setTextViewText(R.id.widget_subject, if (lessons.isEmpty()) "Уроків сьогодні немає" else "Уроки завершено")
                    v.setTextViewText(R.id.widget_room, "Гарного дня!")
                    v.setProgressBar(R.id.widget_progress, 100, 0, false)
                    v.setTextViewText(R.id.widget_time, "Розклад на тиждень — у застосунку")
                    v.setTextViewText(R.id.widget_countdown, "Натисни, щоб відкрити")
                }
            }
            val pi = PendingIntent.getActivity(c, id, Intent(c, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            v.setOnClickPendingIntent(R.id.widget_root, pi)
            manager.updateAppWidget(id, v)
        }
    }
}
