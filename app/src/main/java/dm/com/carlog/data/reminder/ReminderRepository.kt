package dm.com.carlog.data.reminder

import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject

class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao
) {
    suspend fun insert(reminder: Reminder): Long {
        return reminderDao.insert(reminder)
    }

    suspend fun update(reminder: Reminder) {
        reminderDao.update(reminder)
    }

    suspend fun delete(reminder: Reminder) {
        reminderDao.delete(reminder)
    }

    fun getByVehicleId(vehicleId: String): Flow<List<Reminder>> {
        return reminderDao.getByVehicleId(vehicleId)
    }

    suspend fun getById(id: String): Reminder? {
        return reminderDao.getById(id)
    }

    fun getOverdueReminders(): Flow<List<Reminder>> {
        return reminderDao.getOverdueReminders()
    }

    fun getOverdueRemindersForVehicle(vehicleId: String): Flow<List<Reminder>> {
        return reminderDao.getOverdueRemindersForVehicle(vehicleId)
    }

    fun getUpcomingReminders(days: Int = 7): Flow<List<Reminder>> {
        val calendar = Calendar.getInstance()
        val startDate = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        val endDate = calendar.timeInMillis
        return reminderDao.getUpcomingReminders(startDate, endDate)
    }

    suspend fun markAsCompleted(id: String) {
        reminderDao.updateCompletionStatus(id, true)
    }

    suspend fun markAsPending(id: String) {
        reminderDao.updateCompletionStatus(id, false)
    }

    suspend fun deleteAllForVehicle(vehicleId: String) {
        reminderDao.deleteAllForVehicle(vehicleId)
    }
}