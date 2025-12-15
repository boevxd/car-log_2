package dm.com.carlog.data.reminder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("SELECT * FROM reminders WHERE vehicle_id = :vehicleId ORDER BY due_date ASC")
    fun getByVehicleId(vehicleId: String): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: String): Reminder?

    @Query("SELECT * FROM reminders WHERE is_completed = 0 AND due_date <= :timestamp ORDER BY due_date ASC")
    fun getOverdueReminders(timestamp: Long = System.currentTimeMillis()): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE is_completed = 0 AND vehicle_id = :vehicleId AND due_date <= :timestamp ORDER BY due_date ASC")
    fun getOverdueRemindersForVehicle(vehicleId: String, timestamp: Long = System.currentTimeMillis()): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE is_completed = 0 AND due_date BETWEEN :startDate AND :endDate ORDER BY due_date ASC")
    fun getUpcomingReminders(startDate: Long, endDate: Long): Flow<List<Reminder>>

    @Query("UPDATE reminders SET is_completed = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: String, isCompleted: Boolean)

    @Query("DELETE FROM reminders WHERE vehicle_id = :vehicleId")
    suspend fun deleteAllForVehicle(vehicleId: String)
}