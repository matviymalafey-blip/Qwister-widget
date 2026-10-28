package ua.qwister.schoolwidget
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class WidgetTickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) = SchoolWidgetProvider.refreshAll(context)
}
