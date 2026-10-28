package ua.qwister.schoolwidget

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import kotlin.math.abs

class MainActivity : Activity() {
    private var page = 0
    private var accent = 0xFF72C7FF.toInt()
    private var dark = true
    private var room = true
    private var progress = true
    private var countdown = true
    private var compact = false
    private lateinit var root: LinearLayout
    private lateinit var pageHost: FrameLayout
    private lateinit var nav: LinearLayout
    private var downX = 0f
    private var downY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadPrefs()
        buildShell()
        showPage()
    }

    private fun loadPrefs() {
        accent = WidgetPrefs.accent(this); dark = WidgetPrefs.dark(this)
        room = WidgetPrefs.showRoom(this); progress = WidgetPrefs.showProgress(this)
        countdown = WidgetPrefs.showCountdown(this); compact = WidgetPrefs.compact(this)
    }

    private fun fg() = if (dark) Color.WHITE else 0xFF202431.toInt()
    private fun muted() = if (dark) 0xFFD4D7E2.toInt() else 0xFF555B6E.toInt()
    private fun bg() = if (dark) 0xFF171923.toInt() else 0xFFF3F5FA.toInt()

    private fun buildShell() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 20, 20, 12); setBackgroundColor(bg()) }
        val header = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        header.addView(label("QWISTER", 24, accent, true))
        header.addView(label("Свайпай ліворуч або праворуч • ${page + 1}/3", 12, muted(), false))
        root.addView(header)
        pageHost = FrameLayout(this)
        root.addView(pageHost, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        pageHost.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { downX = e.x; downY = e.y; true }
                MotionEvent.ACTION_UP -> {
                    val dx = e.x - downX; val dy = e.y - downY
                    if (abs(dx) > 75 && abs(dx) > abs(dy) * 1.2f) {
                        page = (page + if (dx < 0) 1 else -1).coerceIn(0, 2)
                        showPage()
                        true
                    } else false
                }
                else -> true
            }
        }
        nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        root.addView(nav)
        setContentView(root)
    }

    private fun showPage() {
        pageHost.removeAllViews()
        nav.removeAllViews()
        val names = listOf("Розклад", "Нотатки", "Калькулятор")
        names.forEachIndexed { index, name ->
            nav.addView(Button(this).apply {
                text = if (index == page) "● $name" else name
                isAllCaps = false
                setOnClickListener { page = index; showPage() }
            }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
        val content = when (page) {
            0 -> timetablePage()
            1 -> notesPage()
            else -> calculatorPage()
        }
        pageHost.addView(content)
        content.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { downX = e.x; downY = e.y; true }
                MotionEvent.ACTION_UP -> {
                    val dx = e.x - downX; val dy = e.y - downY
                    if (abs(dx) > 75 && abs(dx) > abs(dy) * 1.2f) {
                        page = (page + if (dx < 0) 1 else -1).coerceIn(0, 2)
                        showPage(); true
                    } else false
                }
                else -> true
            }
        }
    }

    private fun timetablePage(): View {
        val scroll = ScrollView(this)
        val box = column()
        box.addView(label("РОЗКЛАД НА ТИЖДЕНЬ", 18, accent, true))
        box.addView(label("Урок — 45 хв • Перерва — 10 хв", 13, muted(), false))
        val now = java.time.LocalTime.now()
        val today = java.time.LocalDate.now().dayOfWeek
        val todays = TimetableData.week[today].orEmpty()
        val current = todays.firstOrNull { !now.isBefore(it.start) && now.isBefore(it.end) }
        val next = todays.firstOrNull { now.isBefore(it.start) }
        box.addView(label("\nЗАРАЗ", 16, accent, true))
        box.addView(label(
            when {
                current != null -> "${current.subject}\n${current.start}–${current.end}" + if (current.room.isBlank()) "" else "\nКабінет ${current.room}"
                next != null -> "Наступний: ${next.subject}\n${next.start}–${next.end}" + if (next.room.isBlank()) "" else "\nКабінет ${next.room}"
                todays.isEmpty() -> "Сьогодні уроків немає"
                else -> "Уроки на сьогодні завершилися"
            }, 17, fg(), true
        ))
        val pbar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = if (current == null) 0 else ((java.time.Duration.between(current.start, now).seconds * 100) /
                java.time.Duration.between(current.start, current.end).seconds.coerceAtLeast(1)).toInt().coerceIn(0, 100)
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
        }
        box.addView(pbar, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 10))
        TimetableData.week.forEach { (day, lessons) ->
            box.addView(label("\n${TimetableData.dayNames[day]}", 16, accent, true))
            lessons.forEachIndexed { i, l ->
                box.addView(label("${i + 1}. ${l.subject}" + if (l.room.isBlank()) "" else " • каб. ${l.room}" +
                    "\n   ${l.start}–${l.end}", 14, fg(), false))
            }
        }
        box.addView(label("\nНАЛАШТУВАННЯ ВІДЖЕТА", 17, accent, true))
        box.addView(button(if (dark) "Перемкнути на світлу тему" else "Перемкнути на темну тему") { dark = !dark; saveAndRebuild() })
        listOf("Блакитний" to 0xFF72C7FF.toInt(), "Фіолетовий" to 0xFFB69CFF.toInt(),
            "Зелений" to 0xFF68D391.toInt(), "Помаранчевий" to 0xFFFFB86B.toInt(),
            "Рожевий" to 0xFFFF8FC7.toInt(), "Червоний" to 0xFFFF7777.toInt()).forEach { (n, c) ->
            box.addView(button((if (accent == c) "✓ " else "") + n) { accent = c; saveAndRebuild() })
        }
        box.addView(check("Показувати кабінет", room) { room = it; saveAndRebuild() })
        box.addView(check("Показувати прогрес-бар", progress) { progress = it; saveAndRebuild() })
        box.addView(check("Показувати відлік до дзвінка", countdown) { countdown = it; saveAndRebuild() })
        box.addView(check("Компактний віджет", compact) { compact = it; saveAndRebuild() })
        scroll.addView(box)
        return scroll
    }

    private fun notesPage(): View {
        val scroll = ScrollView(this)
        val box = column()
        box.addView(label("НОТАТКИ", 20, accent, true))
        box.addView(label("Записуй домашні завдання, що взяти на урок або нагадування. Нотатки зберігаються на телефоні.", 14, muted(), false))
        val editor = EditText(this).apply {
            hint = "Наприклад:\nАлгебра — № 245, 247\nІсторія — повторити параграф"
            setTextColor(fg()); setHintTextColor(muted())
            setPadding(14, 14, 14, 14)
            minLines = 7; gravity = Gravity.TOP
            setBackgroundColor(if (dark) 0xFF252936.toInt() else Color.WHITE)
            setText(getSharedPreferences("qwister_notes", MODE_PRIVATE).getString("notes", ""))
        }
        box.addView(editor, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        box.addView(button("Зберегти нотатки") {
            getSharedPreferences("qwister_notes", MODE_PRIVATE).edit().putString("notes", editor.text.toString()).apply()
            Toast.makeText(this, "Нотатки збережено на телефоні", Toast.LENGTH_SHORT).show()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editor.windowToken, 0)
        })
        box.addView(button("Очистити нотатки") {
            editor.setText("")
            getSharedPreferences("qwister_notes", MODE_PRIVATE).edit().remove("notes").apply()
            Toast.makeText(this, "Нотатки очищено", Toast.LENGTH_SHORT).show()
        })
        scroll.addView(box)
        return scroll
    }

    private fun calculatorPage(): View {
        val box = column()
        box.addView(label("КАЛЬКУЛЯТОР", 20, accent, true))
        box.addView(label("Простий калькулятор для швидких обчислень.", 14, muted(), false))
        val display = EditText(this).apply {
            hint = "Наприклад: 125 * 4 + 8"
            setTextColor(fg()); setHintTextColor(muted()); textSize = 22f
            setSingleLine(true); inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        box.addView(display, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        val result = label("Результат з’явиться тут", 20, fg(), true)
        box.addView(result)
        val grid = GridLayout(this).apply { columnCount = 4 }
        val keys = listOf("7","8","9","÷","4","5","6","×","1","2","3","−","C","0",".","+","(",")","⌫","=")
        keys.forEach { key ->
            grid.addView(Button(this).apply {
                text = key; isAllCaps = false
                setOnClickListener {
                    val old = display.text.toString()
                    when (key) {
                        "C" -> { display.setText(""); result.text = "Результат з’явиться тут" }
                        "⌫" -> if (old.isNotEmpty()) display.setText(old.dropLast(1))
                        "=" -> {
                            val answer = evaluate(old)
                            result.text = answer ?: "Перевір вираз"
                        }
                        else -> {
                            val add = when (key) { "×" -> "*"; "÷" -> "/"; "−" -> "-"; else -> key }
                            display.setText(old + add); display.setSelection(display.text.length)
                        }
                    }
                }
            }, GridLayout.LayoutParams().apply {
                width = 0; height = ViewGroup.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(2, 2, 2, 2)
            })
        }
        box.addView(grid)
        return box
    }

    // Small recursive-descent calculator: + - * / and parentheses, without eval or network access.
    private fun evaluate(input: String): String? = try {
        val parser = ExpressionParser(input.replace("×", "*").replace("÷", "/").replace("−", "-"))
        val value = parser.parse()
        if (!value.isFinite()) null else if (value % 1.0 == 0.0) value.toLong().toString() else "%.6f".format(value).trimEnd('0').trimEnd('.')
    } catch (_: Exception) { null }

    private class ExpressionParser(private val s: String) {
        private var i = 0
        fun parse(): Double {
            val v = expr()
            spaces()
            if (i != s.length) error("Unexpected")
            return v
        }
        private fun spaces() { while (i < s.length && s[i].isWhitespace()) i++ }
        private fun eat(c: Char): Boolean { spaces(); if (i < s.length && s[i] == c) { i++; return true }; return false }
        private fun expr(): Double {
            var v = term()
            while (true) v = when { eat('+') -> v + term(); eat('-') -> v - term(); else -> return v }
        }
        private fun term(): Double {
            var v = factor()
            while (true) v = when {
                eat('*') -> v * factor()
                eat('/') -> { val d = factor(); if (d == 0.0) error("Divide by zero"); v / d }
                else -> return v
            }
        }
        private fun factor(): Double {
            spaces()
            if (eat('+')) return factor()
            if (eat('-')) return -factor()
            if (eat('(')) { val v = expr(); if (!eat(')')) error("Missing )"); return v }
            spaces()
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
            if (start == i) error("Number expected")
            return s.substring(start, i).toDouble()
        }
    }

    private fun saveAndRebuild() {
        WidgetPrefs.save(this, accent, dark, room, progress, countdown, compact)
        SchoolWidgetProvider.refreshAll(this)
        buildShell()
        showPage()
    }

    private fun column() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 8, 0, 16)
    }
    private fun label(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        setPadding(0, 7, 0, 7)
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    private fun button(value: String, action: () -> Unit) = Button(this).apply {
        text = value; isAllCaps = false; setOnClickListener { action() }
    }
    private fun check(value: String, checked: Boolean, action: (Boolean) -> Unit) = CheckBox(this).apply {
        text = value; isChecked = checked; setTextColor(fg())
        setOnCheckedChangeListener { _, state -> action(state) }
    }
}
