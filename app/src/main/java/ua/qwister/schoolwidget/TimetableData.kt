package ua.qwister.schoolwidget

import java.time.DayOfWeek
import java.time.LocalTime

data class Lesson(val subject: String, val room: String = "", val start: LocalTime, val end: LocalTime)

object TimetableData {
    const val LESSON_MINUTES = 45
    const val BREAK_MINUTES = 10
    val periods: List<Pair<LocalTime, LocalTime>> = (0 until 8).map { i ->
        val startMinute = 8 * 60 + i * (LESSON_MINUTES + BREAK_MINUTES)
        val start = LocalTime.of(startMinute / 60, startMinute % 60)
        start to start.plusMinutes(LESSON_MINUTES.toLong())
    }
    private fun lesson(i: Int, subject: String, room: String = ""): Lesson =
        Lesson(subject, room, periods[i].first, periods[i].second)

    val week: Map<DayOfWeek, List<Lesson>> = mapOf(
        DayOfWeek.MONDAY to listOf(
            lesson(0, "Алгебра", "402"), lesson(1, "Геометрія", "402"), lesson(2, "Українська мова", "307"),
            lesson(3, "Українська література", "307"), lesson(4, "Зарубіжна література", "305"),
            lesson(5, "Інформатика"), lesson(6, "Технології")
        ),
        DayOfWeek.TUESDAY to listOf(
            lesson(0, "Алгебра", "402"), lesson(1, "Хімія", "308"), lesson(2, "Історія", "403"),
            lesson(3, "Англійська мова", "306/409"), lesson(4, "Англійська мова", "306/409"),
            lesson(5, "Біологія", "405"), lesson(6, "Українська мова", "407")
        ),
        DayOfWeek.WEDNESDAY to listOf(
            lesson(0, "Біологія", "405"), lesson(1, "Алгебра / геометрія", "402"), lesson(2, "Фізкультура"),
            lesson(3, "Хімія", "302"), lesson(4, "Фізика", "305"), lesson(5, "Англійська мова", "308/109"),
            lesson(6, "Англійська мова", "308/109")
        ),
        DayOfWeek.THURSDAY to listOf(
            lesson(0, "Навч. разом", "407"), lesson(1, "Музичне мистецтво", "216"),
            lesson(2, "Зарубіжна література", "305"), lesson(3, "Фізкультура"), lesson(4, "Українська мова", "307"),
            lesson(5, "Українська література", "307"), lesson(6, "Географія", "201"), lesson(7, "Виховна година")
        ),
        DayOfWeek.FRIDAY to listOf(
            lesson(0, "Алгебра", "402"), lesson(1, "Історія", "403"), lesson(2, "Геометрія", "402"),
            lesson(3, "Географія", "306"), lesson(4, "Фізика", "405"), lesson(5, "Англійська мова", "109/408"),
            lesson(6, "Історія", "403"), lesson(7, "Фізкультура")
        )
    )
    val dayNames = mapOf(
        DayOfWeek.MONDAY to "ПОНЕДІЛОК", DayOfWeek.TUESDAY to "ВІВТОРОК",
        DayOfWeek.WEDNESDAY to "СЕРЕДА", DayOfWeek.THURSDAY to "ЧЕТВЕР",
        DayOfWeek.FRIDAY to "П’ЯТНИЦЯ", DayOfWeek.SATURDAY to "СУБОТА", DayOfWeek.SUNDAY to "НЕДІЛЯ"
    )
}
