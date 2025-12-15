package dm.com.carlog.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dm.com.carlog.data.reminder.ReminderType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    showDialog: Boolean,
    title: String = "",
    description: String = "",
    selectedDate: Long = System.currentTimeMillis(),
    selectedType: ReminderType = ReminderType.MAINTENANCE,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, ReminderType, Long) -> Unit,
    showDatePickerDialog: () -> Unit // Без значения по умолчанию
) {
    var localTitle by remember { mutableStateOf(title) }
    var localDescription by remember { mutableStateOf(description) }
    var localType by remember { mutableStateOf(selectedType) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val formattedDate = remember(selectedDate) {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate))
    }

    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = if (title.isEmpty()) "Add Reminder" else "Edit Reminder",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = localTitle,
                        onValueChange = { localTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = localTitle.isBlank()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = localDescription,
                        onValueChange = { localDescription = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Type:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = localType.name,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text("Select type") }
                        )

                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            ReminderType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        localType = type
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Due Date:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Button(
                        onClick = showDatePickerDialog,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Filled.DateRange,
                            contentDescription = "Select Date",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(formattedDate)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (localTitle.isNotBlank()) {
                                    onConfirm(
                                        localTitle,
                                        localDescription.ifBlank { null },
                                        localType,
                                        selectedDate
                                    )
                                }
                            },
                            enabled = localTitle.isNotBlank()
                        ) {
                            Text(if (title.isEmpty()) "Add" else "Save")
                        }
                    }
                }
            }
        }
    }
}

