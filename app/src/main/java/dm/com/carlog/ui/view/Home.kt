package dm.com.carlog.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import dm.com.carlog.R
import dm.com.carlog.data.vehicle.Vehicle
import dm.com.carlog.data.vehicle.VehicleWithStats
import dm.com.carlog.ui.component.SectionDivider
import dm.com.carlog.ui.component.VehicleItem
import dm.com.carlog.ui.theme.CarLogTheme
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun Home(
    vehiclesWithStats: List<VehicleWithStats>,
    onAddVehicleClick: () -> Unit,
    onEditVehicleClick: (Vehicle) -> Unit,
    onDeleteVehicleClick: (Vehicle) -> Unit,
    onVehicleClick: (VehicleWithStats) -> Unit,
    shouldExit: Boolean,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var isHeaderVisible by remember { mutableStateOf(false) }
    var areItemsVisible by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(shouldExit) {
        if (shouldExit) {
            areItemsVisible = false
            kotlinx.coroutines.delay(300)
            isHeaderVisible = false
        } else {
            isHeaderVisible = true
            kotlinx.coroutines.delay(300)
            areItemsVisible = true
        }
    }

    val fabScale by animateFloatAsState(
        targetValue = if (areItemsVisible && !shouldExit) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    // Кнопка настроек
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddVehicleClick,
                modifier = Modifier.scale(fabScale),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.fab_icon)
                    )
                },
                text = { Text(text = stringResource(R.string.add_vehicle)) },
                shape = MaterialTheme.shapes.medium
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(dimensionResource(R.dimen.padding_medium))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
            ) {
                AnimatedVisibility(
                    visible = isHeaderVisible && !shouldExit,
                    enter = slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 600)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 400)
                    )
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium))
                    ) {
                        SectionDivider(
                            title = R.string.your_vehicles
                        )
                    }
                }

                AnimatedVisibility(
                    visible = areItemsVisible && !shouldExit,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 600)
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 400)
                    )
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_medium)),
                        state = lazyListState
                    ) {
                        items(
                            items = vehiclesWithStats,
                            key = { it.vehicle.id }
                        ) { vehicleWithStats ->
                            VehicleItem(
                                vehicleWithStats = vehicleWithStats,
                                onClick = { onVehicleClick(vehicleWithStats) },
                                onEditClick = { onEditVehicleClick(vehicleWithStats.vehicle) },
                                onDeleteClick = { onDeleteVehicleClick(vehicleWithStats.vehicle) },
                                modifier = Modifier
                            )
                        }

                        if (vehiclesWithStats.isEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.nothing_to_see_here),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        item {
                            Spacer(
                                modifier = Modifier
                                    .padding(bottom = dimensionResource(R.dimen.bottom_spacer))
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun PreviewHome() {
    CarLogTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Home(
                vehiclesWithStats = listOf(
                    VehicleWithStats(
                        vehicle = Vehicle(
                            id = "1",
                            name = "My Car",
                            manufacturer = "Toyota",
                            model = "Avanza",
                            year = 2010,
                            licensePlate = "020-111",
                            vin = "862346ajdaaaaa"
                        ),
                        latestOdometer = 150000,
                        averageFuelEconomy = 14.5,
                        totalFuelAdded = 400.0,
                        totalSpent = 12000000.0,
                        refuelCount = 40,
                        refuelPerMonth = 3.0,
                        avgGallonRefueled = 7.0,
                        avgSpentPerRefuel = 85000.0
                    )
                ),
                onAddVehicleClick = {},
                onEditVehicleClick = {},
                onDeleteVehicleClick = {},
                onVehicleClick = {},
                shouldExit = false,
                onSettingsClick = {}
            )
        }
    }
}