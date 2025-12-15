package dm.com.carlog.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dm.com.carlog.data.expense.ExpenseCategory
import dm.com.carlog.data.expense.ExpenseCategoryDao
import dm.com.carlog.data.fuel.Fuel
import dm.com.carlog.data.fuel.FuelDao
import dm.com.carlog.data.reminder.Reminder
import dm.com.carlog.data.reminder.ReminderDao
import dm.com.carlog.data.vehicle.Vehicle
import dm.com.carlog.data.vehicle.VehicleDao

@Database(
    entities = [
        Vehicle::class,
        Fuel::class,
        ExpenseCategory::class,
        Reminder::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelDao(): FuelDao
    abstract fun expenseCategoryDao(): ExpenseCategoryDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        const val DATABASE_NAME = "carlog.db"
    }
}