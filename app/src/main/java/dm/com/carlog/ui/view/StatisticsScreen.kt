package dm.com.carlog.ui.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dm.com.carlog.R
import dm.com.carlog.model.StatisticsViewModel
import dm.com.carlog.ui.component.StatisticsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit = {}
) {
    val viewModel: StatisticsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.statistics_and_analytics)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.yearly_overview),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticsCard(
                        title = stringResource(R.string.total_spent),
                        value = "$${String.format("%.2f", uiState.yearlyOverview.totalSpent)}",
                        icon = { Icon(Icons.Filled.Assessment, contentDescription = stringResource(R.string.total_spent)) },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )

                    StatisticsCard(
                        title = stringResource(R.string.total_fuel),
                        value = "${String.format("%.1f", uiState.yearlyOverview.totalFuel)} L",
                        icon = { Icon(Icons.Filled.ShowChart, contentDescription = stringResource(R.string.total_fuel)) },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticsCard(
                        title = stringResource(R.string.avg_economy),
                        value = "${String.format("%.1f", uiState.yearlyOverview.averageEconomy)} mpg",
                        icon = { Icon(Icons.Filled.TrendingUp, contentDescription = stringResource(R.string.avg_economy)) },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    StatisticsCard(
                        title = stringResource(R.string.total_trips),
                        value = uiState.yearlyOverview.totalTrips.toString(),
                        icon = { Icon(Icons.Filled.PieChart, contentDescription = stringResource(R.string.total_trips)) },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (uiState.monthlyData.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.monthly_breakdown),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                items(uiState.monthlyData) { monthlyStat ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "${monthlyStat.month} ${monthlyStat.year}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.total_spent),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", monthlyStat.totalSpent)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.total_fuel),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${String.format("%.1f", monthlyStat.totalFuel)} L",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.avg_economy),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${String.format("%.1f", monthlyStat.averageEconomy)} mpg",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_statistics_available),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}