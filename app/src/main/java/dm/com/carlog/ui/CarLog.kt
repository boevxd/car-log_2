package dm.com.carlog.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dm.com.carlog.data.reminder.ReminderType
import dm.com.carlog.model.*
import dm.com.carlog.ui.component.DeleteBottomSheet
import dm.com.carlog.ui.component.FuelFormBottomSheet
import dm.com.carlog.ui.component.ReminderDialog
import dm.com.carlog.ui.component.SettingsScreen
import dm.com.carlog.ui.component.VehicleFormBottomSheet
import dm.com.carlog.ui.view.Detail
import dm.com.carlog.ui.view.Home
import dm.com.carlog.ui.view.RemindersScreen
import dm.com.carlog.ui.view.StatisticsScreen
import dm.com.carlog.util.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CarLogNav {
    HOME, VEHICLE_DETAIL, SETTINGS, STATISTICS, REMINDERS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarLog() {
    val navController: NavHostController = rememberNavController()
    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()

    val vehicleViewModel: VehicleViewModel = hiltViewModel()
    val vehicleFormViewModel: VehicleFormViewModel = hiltViewModel()
    val fuelViewModel: FuelViewModel = hiltViewModel()
    val fuelFormViewModel: FuelFormViewModel = hiltViewModel()
    val deleteViewModel: DeleteViewModel = hiltViewModel()
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    val reminderViewModel: ReminderViewModel = hiltViewModel()

    val vehicleUiState by vehicleViewModel.uiState.collectAsState()
    val vehicleFormUiState by vehicleFormViewModel.uiState.collectAsState()
    val fuelUiState by fuelViewModel.uiState.collectAsState()
    val fuelFormUiState by fuelFormViewModel.uiState.collectAsState()
    val deleteUiState by deleteViewModel.uiState.collectAsState()
    val reminderUiState by reminderViewModel.uiState.collectAsState()

    val vehicleSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val fuelSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val deleteSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        CarLogNav.HOME.name,
        CarLogNav.STATISTICS.name,
        CarLogNav.REMINDERS.name
    )

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        val items = listOf(
                            BottomNavItem(
                                route = CarLogNav.HOME.name,
                                title = "Home",
                                icon = Icons.Filled.Home
                            ),
                            BottomNavItem(
                                route = CarLogNav.STATISTICS.name,
                                title = "Statistics",
                                icon = Icons.Filled.Assessment
                            ),
                            BottomNavItem(
                                route = CarLogNav.REMINDERS.name,
                                title = "Reminders",
                                icon = Icons.Filled.CarRepair
                            )
                        )

                        items.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        // Очищаем бэкстэк до корня при навигации по табам
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = CarLogNav.HOME.name,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(paddingValues)
            ) {
                composable(route = CarLogNav.HOME.name) {
                    var shouldExit by remember { mutableStateOf(false) }

                    Home(
                        vehiclesWithStats = vehicleUiState.vehiclesWithStats,
                        onAddVehicleClick = {
                            vehicleFormViewModel.showSheet()
                        },
                        onEditVehicleClick = { vehicle ->
                            vehicleFormViewModel.setupEdit(vehicle)
                            vehicleFormViewModel.showSheet()
                        },
                        onDeleteVehicleClick = { vehicle ->
                            deleteViewModel.setIsOnDetails(false)
                            deleteViewModel.updateSelectedVehicle(vehicle)
                            deleteViewModel.showSheet()
                        },
                        onVehicleClick = { vehicleWithStats ->
                            shouldExit = true
                            scope.launch {
                                delay(300)
                                fuelFormViewModel.clearEdit()
                                vehicleViewModel.updateSelectedVehicleWithStats(vehicleWithStats)
                                fuelViewModel.populateFuels(vehicleWithStats.vehicle.id)
                                navController.navigate(CarLogNav.VEHICLE_DETAIL.name)
                            }
                        },
                        shouldExit = shouldExit,
                        onSettingsClick = {
                            scope.launch {
                                delay(300)
                                navController.navigate(CarLogNav.SETTINGS.name)
                            }
                        }
                    )
                }

                composable(route = CarLogNav.VEHICLE_DETAIL.name) {
                    if (vehicleUiState.selectedVehicleWithStats == null) {
                        navController.popBackStack()
                        showToast(context, "Selected vehicle not found", true)
                        return@composable
                    }

                    var shouldExit by remember { mutableStateOf(false) }

                    BackHandler {
                        shouldExit = true
                        scope.launch {
                            delay(300)
                            navController.popBackStack()
                        }
                    }

                    Detail(
                        onBackClick = {
                            shouldExit = true
                            scope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        },
                        onEditClick = {
                            vehicleFormViewModel.setupEdit(vehicleUiState.selectedVehicleWithStats!!.vehicle)
                            vehicleFormViewModel.showSheet()
                        },
                        onDeleteClick = {
                            deleteViewModel.setIsOnDetails(true)
                            deleteViewModel.updateSelectedVehicle(vehicleUiState.selectedVehicleWithStats!!.vehicle)
                            deleteViewModel.showSheet()
                        },
                        onExportVehicleClick = {
                            vehicleFormViewModel.exportVehicleData(
                                context,
                                vehicleUiState.selectedVehicleWithStats!!.vehicle
                            )
                        },
                        vehicleWithStats = vehicleUiState.selectedVehicleWithStats!!,
                        fuels = fuelUiState.fuels,
                        onAddFuelClick = {
                            fuelFormViewModel.showSheet()
                        },
                        onFuelEditClick = { fuel ->
                            fuelFormViewModel.setupEdit(fuel)
                            fuelFormViewModel.showSheet()
                        },
                        onFuelDeleteClick = { fuel ->
                            deleteViewModel.updateSelectedFuel(fuel)
                            deleteViewModel.setDeleteType(DeleteType.FUEL)
                            deleteViewModel.showSheet()
                        },
                        shouldExit = shouldExit,
                        onViewStatistics = {
                            navController.navigate(CarLogNav.STATISTICS.name)
                        },
                        onViewReminders = {
                            navController.navigate(CarLogNav.REMINDERS.name)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                composable(route = CarLogNav.SETTINGS.name) {
                    var shouldExit by remember { mutableStateOf(false) }

                    BackHandler {
                        shouldExit = true
                        scope.launch {
                            delay(300)
                            navController.popBackStack()
                        }
                    }

                    SettingsScreen(
                        onBackClick = {
                            shouldExit = true
                            scope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        },
                        shouldExit = shouldExit
                    )
                }

                composable(route = CarLogNav.STATISTICS.name) {
                    var shouldExit by remember { mutableStateOf(false) }

                    BackHandler {
                        shouldExit = true
                        scope.launch {
                            delay(300)
                            navController.popBackStack()
                        }
                    }

                    StatisticsScreen(
                        onBackClick = {
                            shouldExit = true
                            scope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable(route = CarLogNav.REMINDERS.name) {
                    var shouldExit by remember { mutableStateOf(false) }

                    BackHandler {
                        shouldExit = true
                        scope.launch {
                            delay(300)
                            navController.popBackStack()
                        }
                    }

                    RemindersScreen(
                        onBackClick = {
                            shouldExit = true
                            scope.launch {
                                delay(300)
                                navController.popBackStack()
                            }
                        }
                    )
                }
            }

            FuelFormBottomSheet(
                modifier = Modifier,
                onDismissRequest = {
                    fuelFormViewModel.hideSheet()
                    if (fuelFormUiState.isEdit) {
                        fuelFormViewModel.clearEdit()
                    }
                },
                onCloseButtonClick = {
                    scope.launch {
                        fuelSheetState.hide()
                        fuelFormViewModel.hideSheet()
                        if (fuelFormUiState.isEdit) {
                            fuelFormViewModel.clearEdit()
                        }
                    }
                },
                onSaveButtonClick = {
                    if (fuelFormUiState.isEdit) {
                        fuelFormViewModel.updateFuel(
                            context = context,
                            vehicle = vehicleUiState.selectedVehicleWithStats!!.vehicle,
                            fuel = fuelFormUiState.selectedFuel!!,
                            previousOdometer = fuelUiState.fuels.filter { fuel ->
                                fuel.date < fuelFormUiState.selectedFuel!!.date || (fuel.date == fuelFormUiState.selectedFuel!!.date && fuel.createdAt < fuelFormUiState.selectedFuel!!.createdAt)
                            }.maxWithOrNull(compareBy({ it.date }, { it.createdAt }))?.odometer,
                            onSuccess = {
                                scope.launch {
                                    fuelViewModel.populateFuels(vehicleUiState.selectedVehicleWithStats!!.vehicle.id)
                                    vehicleViewModel.updateSelectedVehicleAfterEdit()
                                    fuelSheetState.hide()
                                    fuelFormViewModel.clearEdit()
                                    fuelFormViewModel.hideSheet()
                                }
                            })
                    } else {
                        fuelFormViewModel.addFuel(
                            context = context,
                            vehicle = vehicleUiState.selectedVehicleWithStats!!.vehicle,
                            previousOdometer = fuelUiState.fuels.maxWithOrNull(
                                compareBy(
                                    { it.date },
                                    { it.createdAt })
                            )?.odometer,
                            onSuccess = {
                                scope.launch {
                                    fuelViewModel.populateFuels(vehicleUiState.selectedVehicleWithStats!!.vehicle.id)
                                    vehicleViewModel.updateSelectedVehicleAfterEdit()
                                    fuelSheetState.hide()
                                    fuelFormViewModel.resetForm()
                                    fuelFormViewModel.hideSheet()
                                }
                            })
                    }
                },
                sheetState = fuelSheetState,
                isProcessing = fuelFormUiState.isProcessing,
                isEdit = fuelFormUiState.isEdit,
                showSheet = fuelFormUiState.showSheet,
                dateValue = fuelFormViewModel.date,
                onDateValueChange = { fuelFormViewModel.updateDate(it) },
                dateError = fuelFormUiState.errorState.dateError,
                odometerValue = fuelFormViewModel.odometer,
                onOdometerValueChange = { fuelFormViewModel.updateOdometer(it) },
                odometerError = fuelFormUiState.errorState.odometerError,
                tripValue = fuelFormViewModel.trip,
                onTripValueChange = { fuelFormViewModel.updateTrip(it) },
                tripError = fuelFormUiState.errorState.tripError,
                fuelAddedValue = fuelFormViewModel.fuelAdded,
                onFuelAddedValueChange = { fuelFormViewModel.updateFuelAdded(it) },
                fuelAddedError = fuelFormUiState.errorState.fuelAddedError,
                pricePerGallonValue = fuelFormViewModel.pricePerGallon,
                onPricePerGallonValueChange = { fuelFormViewModel.updatePricePerGallon(it) },
                pricePerGallonError = fuelFormUiState.errorState.pricePerGallonError,
                canCalculateTrip = fuelFormViewModel.odometer.isNotEmpty() && fuelUiState.fuels.isNotEmpty() &&
                        (if (fuelFormUiState.isEdit)
                            fuelUiState.fuels.any { fuel ->
                                fuel.date < fuelFormUiState.selectedFuel!!.date ||
                                        (fuel.date == fuelFormUiState.selectedFuel!!.date && fuel.createdAt < fuelFormUiState.selectedFuel!!.createdAt)
                            }
                        else true),
                onCanCalculateTripClick = {
                    val previousOdometer = if (fuelFormUiState.isEdit) {
                        fuelUiState.fuels.filter { fuel ->
                            fuel.date < fuelFormUiState.selectedFuel!!.date || (fuel.date == fuelFormUiState.selectedFuel!!.date && fuel.createdAt < fuelFormUiState.selectedFuel!!.createdAt)
                        }.maxWithOrNull(compareBy({ it.date }, { it.createdAt }))?.odometer
                    } else {
                        fuelUiState.fuels.maxWithOrNull(
                            compareBy(
                                { it.date },
                                { it.createdAt })
                        )?.odometer
                    }
                    fuelFormViewModel.calculateTripFromPreviousOdometer(previousOdometer)
                }
            )

            VehicleFormBottomSheet(
                onDismissRequest = {
                    vehicleFormViewModel.hideSheet()
                    if (vehicleFormUiState.isEdit) vehicleFormViewModel.clearEdit()
                },
                onCloseButtonClick = {
                    scope.launch {
                        vehicleSheetState.hide()
                        vehicleFormViewModel.hideSheet()
                        if (vehicleFormUiState.isEdit) vehicleFormViewModel.clearEdit()
                    }
                },
                onSaveButtonClick = {
                    if (vehicleFormUiState.isEdit) {
                        vehicleFormViewModel.updateVehicle(
                            context = context,
                            vehicle = vehicleFormUiState.selectedVehicle!!,
                            onSuccess = {
                                scope.launch {
                                    vehicleViewModel.updateSelectedVehicleAfterEdit()
                                    vehicleSheetState.hide()
                                    vehicleFormViewModel.clearEdit()
                                    vehicleFormViewModel.hideSheet()
                                }
                            })
                    } else {
                        vehicleFormViewModel.addVehicle(context = context, onSuccess = {
                            scope.launch {
                                vehicleSheetState.hide()
                                vehicleFormViewModel.resetForm()
                                vehicleFormViewModel.hideSheet()
                            }
                        })
                    }
                },
                sheetState = vehicleSheetState,
                isProcessing = vehicleFormUiState.isProcessing,
                isEdit = vehicleFormUiState.isEdit,
                showSheet = vehicleFormUiState.showSheet,
                nameValue = vehicleFormViewModel.name,
                onNameValueChange = { vehicleFormViewModel.updateName(it) },
                nameError = vehicleFormUiState.errorState.nameError,
                manufacturerValue = vehicleFormViewModel.manufacturer,
                onManufacturerValueChange = { vehicleFormViewModel.updateManufacturer(it) },
                manufacturerError = vehicleFormUiState.errorState.manufacturerError,
                modelValue = vehicleFormViewModel.model,
                onModelValueChange = { vehicleFormViewModel.updateModel(it) },
                modelError = vehicleFormUiState.errorState.modelError,
                yearValue = vehicleFormViewModel.year,
                onYearValueChange = { vehicleFormViewModel.updateYear(it) },
                yearError = vehicleFormUiState.errorState.yearError,
                licensePlateValue = vehicleFormViewModel.license_plate,
                onLicensePlateValueChange = { vehicleFormViewModel.updateLicensePlate(it) },
                licensePlateError = null,
                vinValue = vehicleFormViewModel.vin,
                onVinValueChange = { vehicleFormViewModel.updateVin(it) }
            )

            DeleteBottomSheet(
                onDismissRequest = {
                    deleteViewModel.hideSheet()
                },
                onCloseButtonClick = {
                    scope.launch {
                        deleteSheetState.hide()
                        deleteViewModel.hideSheet()
                    }
                },
                onDeleteClick = {
                    if (deleteUiState.deleteType == DeleteType.VEHICLE) {
                        deleteViewModel.deleteVehicle(
                            context = context,
                            vehicle = deleteUiState.selectedVehicle!!,
                            onSuccess = {
                                scope.launch {
                                    if (deleteViewModel.isOnDetails) {
                                        deleteViewModel.setIsOnDetails(false)
                                        navController.popBackStack()
                                    }

                                    deleteSheetState.hide()
                                    deleteViewModel.clear()
                                    deleteViewModel.hideSheet()
                                }
                            })
                    } else {
                        deleteViewModel.deleteFuel(
                            context = context,
                            fuel = deleteUiState.selectedFuel!!,
                            onSuccess = {
                                scope.launch {
                                    vehicleViewModel.updateSelectedVehicleAfterEdit()
                                    fuelViewModel.removeFromList(deleteUiState.selectedFuel!!)
                                    deleteSheetState.hide()
                                    deleteViewModel.clear()
                                    deleteViewModel.hideSheet()
                                }
                            })
                    }
                },
                sheetState = deleteSheetState,
                isProcessing = deleteUiState.isProcessing,
                showSheet = deleteUiState.showSheet,
                name = deleteUiState.name
            )

            if (reminderUiState.showDialog) {
                ReminderDialog(
                    showDialog = true,
                    title = reminderUiState.selectedReminder?.title ?: "",
                    description = reminderUiState.selectedReminder?.description ?: "",
                    selectedDate = reminderUiState.selectedReminder?.dueDate ?: System.currentTimeMillis(),
                    selectedType = reminderUiState.selectedReminder?.type ?: ReminderType.MAINTENANCE,
                    onDismiss = { reminderViewModel.hideDialog() },
                    onConfirm = { title, description, type, dueDate ->
                        val vehicleId = reminderUiState.selectedVehicleId ?:
                        vehicleUiState.selectedVehicleWithStats?.vehicle?.id

                        if (vehicleId == null) {
                            showToast(context, "No vehicle selected", true)
                            reminderViewModel.hideDialog()
                            return@ReminderDialog
                        }

                        if (reminderUiState.isEditing) {
                            val currentReminder = reminderUiState.selectedReminder
                            if (currentReminder != null) {
                                reminderViewModel.updateReminder(
                                    context = context,
                                    reminder = currentReminder.copy(
                                        title = title,
                                        description = description,
                                        type = type,
                                        dueDate = dueDate
                                    ),
                                    onSuccess = {
                                        reminderViewModel.hideDialog()
                                        reminderViewModel.loadReminders(vehicleId)
                                    }
                                )
                            } else {
                                showToast(context, "No reminder selected for editing", true)
                                reminderViewModel.hideDialog()
                            }
                        } else {
                            reminderViewModel.addReminder(
                                context = context,
                                vehicleId = vehicleId,
                                title = title,
                                description = description,
                                type = type,
                                dueDate = dueDate,
                                onSuccess = {
                                    reminderViewModel.hideDialog()
                                    reminderViewModel.loadReminders(vehicleId)
                                }
                            )
                        }
                    },
                    showDatePickerDialog = {}
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)