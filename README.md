# QWISTER School Widget v0.3

Android timetable widget + swipeable companion app.

## App pages
Swipe left/right between:
1. **Розклад** — today's lessons and full weekly timetable.
2. **Нотатки** — save notes locally, for example homework for each lesson.
3. **Калькулятор** — basic arithmetic calculator.
4. Mini-game is intentionally deferred for a later version.

The screen also has navigation buttons, so swiping is not required for accessibility.

## Widget customization
- Dark/light theme.
- Six accent colors.
- Toggle room, progress bar, countdown.
- Compact mode.
- Resizable Android home-screen widget.
- Progress recalculated from current device time, refreshed approximately once a minute while widget is active. Android may delay background updates due to Doze and battery saver.

## Bell schedule
Based on the user's rule: lesson = 45 minutes, break = 10 minutes, first lesson starts at 08:00.
1. 08:00–08:45
2. 08:55–09:40
3. 09:50–10:35
4. 10:45–11:30
5. 11:40–12:25
6. 12:35–13:20
7. 13:30–14:15
8. 14:25–15:10

This generated schedule differs from the bell schedule in the supplied school photo. Confirm which schedule your class actually follows before relying on it.

## Build APK
Open in Android Studio, allow Gradle sync, then **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
Debug APK usually appears at `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions
A workflow at `.github/workflows/build-apk.yml` builds a debug APK on push to `main` or manual dispatch. Upload the folder contents to a GitHub repository, then open Actions and download the artifact from the successful run.
