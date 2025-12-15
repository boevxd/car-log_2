package dm.com.carlog.data.reminder

enum class ReminderType(val displayName: String) {
    MAINTENANCE("Maintenance"),
    INSURANCE("Insurance"),
    INSPECTION("Inspection"),
    TAX("Tax"),
    CUSTOM("Custom");

    companion object {
        fun fromDisplayName(displayName: String): ReminderType {
            return values().find { it.displayName == displayName } ?: CUSTOM
        }
    }
}