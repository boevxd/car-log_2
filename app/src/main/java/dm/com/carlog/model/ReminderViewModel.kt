package dm.com.carlog.model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dm.com.carlog.data.reminder.Reminder
import dm.com.carlog.data.reminder.ReminderRepository
import dm.com.carlog.data.reminder.ReminderType
import dm.com.carlog.util.showToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class ReminderState(
    val reminders: List<Reminder> = emptyList(),
    val overdueReminders: List<Reminder> = emptyList(),
    val upcomingReminders: List<Reminder> = emptyList(),
    val selectedReminder: Reminder? = null,
    val showDialog: Boolean = false,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val selectedVehicleId: String? = null
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReminderState())
    val uiState: StateFlow<ReminderState> = _uiState.asStateFlow()

    fun loadReminders(vehicleId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                if (vehicleId != null) {
                    reminderRepository.getByVehicleId(vehicleId).collect { reminders ->
                        val overdue = reminders.filter { !it.isCompleted && it.dueDate <= System.currentTimeMillis() }
                        val upcoming = reminders.filter { !it.isCompleted && it.dueDate > System.currentTimeMillis() }

                        _uiState.update {
                            it.copy(
                                reminders = reminders,
                                overdueReminders = overdue,
                                upcomingReminders = upcoming,
                                selectedVehicleId = vehicleId,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    reminderRepository.getOverdueReminders().collect { overdue ->
                        reminderRepository.getUpcomingReminders(30).collect { upcoming ->
                            _uiState.update {
                                it.copy(
                                    overdueReminders = overdue,
                                    upcomingReminders = upcoming,
                                    isLoading = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showAddDialog() {
        _uiState.update { it.copy(showDialog = true, isEditing = false, selectedReminder = null) }
    }

    fun showEditDialog(reminder: Reminder) {
        _uiState.update { it.copy(showDialog = true, isEditing = true, selectedReminder = reminder) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showDialog = false) }
    }

    fun addReminder(
        context: Context,
        vehicleId: String,
        title: String,
        description: String?,
        type: ReminderType,
        dueDate: Long,
        odometerDue: Int? = null,
        currentOdometer: Int = 0,
        repeatIntervalDays: Int? = null,
        notificationEnabled: Boolean = true,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val reminder = Reminder(
                    id = UUID.randomUUID().toString(),
                    vehicleId = vehicleId,
                    title = title,
                    description = description,
                    type = type,
                    dueDate = dueDate,
                    odometerDue = odometerDue,
                    currentOdometer = currentOdometer,
                    repeatIntervalDays = repeatIntervalDays,
                    notificationEnabled = notificationEnabled
                )

                reminderRepository.insert(reminder)

                showToast(context, "Reminder added successfully")
                loadReminders(vehicleId)
                onSuccess()
            } catch (e: Exception) {
                showToast(context, "Error adding reminder: ${e.message}", true)
            }
        }
    }

    fun updateReminder(
        context: Context,
        reminder: Reminder,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                reminderRepository.update(reminder)

                showToast(context, "Reminder updated successfully")
                loadReminders(reminder.vehicleId)
                onSuccess()
            } catch (e: Exception) {
                showToast(context, "Error updating reminder: ${e.message}", true)
            }
        }
    }

    fun deleteReminder(
        context: Context,
        reminder: Reminder,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                reminderRepository.delete(reminder)

                showToast(context, "Reminder deleted successfully")
                loadReminders(reminder.vehicleId)
                onSuccess()
            } catch (e: Exception) {
                showToast(context, "Error deleting reminder: ${e.message}", true)
            }
        }
    }

    fun markAsCompleted(
        context: Context,
        reminderId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                reminderRepository.markAsCompleted(reminderId)

                showToast(context, "Reminder marked as completed")
                loadReminders(_uiState.value.selectedVehicleId)
                onSuccess()
            } catch (e: Exception) {
                showToast(context, "Error updating reminder: ${e.message}", true)
            }
        }
    }

    fun markAsPending(
        context: Context,
        reminderId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                reminderRepository.markAsPending(reminderId)

                showToast(context, "Reminder marked as pending")
                loadReminders(_uiState.value.selectedVehicleId)
                onSuccess()
            } catch (e: Exception) {
                showToast(context, "Error updating reminder: ${e.message}", true)
            }
        }
    }

    fun createDefaultMaintenanceReminders(
        context: Context,
        vehicleId: String,
        vehicleName: String,
        currentOdometer: Int
    ) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()

                addReminder(
                    context = context,
                    vehicleId = vehicleId,
                    title = "Oil Change for $vehicleName",
                    description = "Regular oil change required",
                    type = ReminderType.MAINTENANCE,
                    dueDate = calendar.apply { add(Calendar.MONTH, 6) }.timeInMillis,
                    odometerDue = currentOdometer + 5000,
                    currentOdometer = currentOdometer,
                    repeatIntervalDays = 180
                )


                addReminder(
                    context = context,
                    vehicleId = vehicleId,
                    title = "Tire Rotation for $vehicleName",
                    description = "Tire rotation required",
                    type = ReminderType.MAINTENANCE,
                    dueDate = calendar.apply { add(Calendar.MONTH, 9) }.timeInMillis,
                    odometerDue = currentOdometer + 7500,
                    currentOdometer = currentOdometer,
                    repeatIntervalDays = 270
                )


                calendar.timeInMillis = System.currentTimeMillis()
                addReminder(
                    context = context,
                    vehicleId = vehicleId,
                    title = "Insurance Renewal for $vehicleName",
                    description = "Vehicle insurance renewal due",
                    type = ReminderType.INSURANCE,
                    dueDate = calendar.apply { add(Calendar.YEAR, 1) }.timeInMillis,
                    repeatIntervalDays = 365
                )

                showToast(context, "Default reminders created")
            } catch (e: Exception) {
                showToast(context, "Error creating default reminders: ${e.message}", true)
            }
        }
    }
}