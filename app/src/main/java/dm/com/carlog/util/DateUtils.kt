package dm.com.carlog.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun getDaysUntil(targetDate: Long): Int {
    val currentTime = System.currentTimeMillis()
    val diff = targetDate - currentTime
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}

fun isDateOverdue(date: Long): Boolean {
    return date < System.currentTimeMillis()
}

fun formatDateWithYear(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun getStartOfMonth(year: Int, month: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1, 0, 0, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

fun getEndOfMonth(year: Int, month: Int): Long {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1, 23, 59, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    calendar.add(Calendar.MONTH, 1)
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    return calendar.timeInMillis
}

fun getMonthName(month: Int): String {
    val sdf = SimpleDateFormat("MMMM", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    return sdf.format(calendar.time)
}

fun getShortMonthName(month: Int): String {
    val sdf = SimpleDateFormat("MMM", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.MONTH, month)
    return sdf.format(calendar.time)
}