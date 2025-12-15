package dm.com.carlog.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dm.com.carlog.data.fuel.Fuel
import dm.com.carlog.data.fuel.FuelRepository
import dm.com.carlog.data.reminder.ReminderRepository
import dm.com.carlog.data.vehicle.VehicleWithStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class StatisticsState(
    val monthlyData: List<MonthlyStatistic> = emptyList(),
    val categoryBreakdown: List<CategoryStatistic> = emptyList(),
    val yearlyOverview: YearlyOverview = YearlyOverview(),
    val isLoading: Boolean = false,
    val selectedVehicleId: String? = null,
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

data class MonthlyStatistic(
    val month: String,
    val year: Int,
    val totalSpent: Double,
    val totalFuel: Double,
    val averageEconomy: Double,
    val fuelCount: Int
)

data class CategoryStatistic(
    val categoryName: String,
    val totalSpent: Double,
    val percentage: Double,
    val count: Int
)

data class YearlyOverview(
    val totalSpent: Double = 0.0,
    val totalFuel: Double = 0.0,
    val averageEconomy: Double = 0.0,
    val totalTrips: Int = 0,
    val totalDistance: Int = 0,
    val fuelStations: Set<String> = emptySet()
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val fuelRepository: FuelRepository,
    private val reminderRepository: ReminderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsState())
    val uiState: StateFlow<StatisticsState> = _uiState.asStateFlow()

    fun loadStatistics(vehicleId: String? = null, year: Int? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val fuels = if (vehicleId != null) {
                    fuelRepository.getByVehicleId(vehicleId)
                } else {
                    fuelRepository.getAll()
                }

                fuels.collect { fuelList ->
                    val selectedYear = year ?: Calendar.getInstance().get(Calendar.YEAR)
                    val filteredFuels = fuelList.filter {
                        val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                        cal.get(Calendar.YEAR) == selectedYear
                    }

                    val monthlyStats = calculateMonthlyStatistics(filteredFuels, selectedYear)
                    val categoryStats = calculateCategoryStatistics(filteredFuels)
                    val yearlyOverview = calculateYearlyOverview(filteredFuels)

                    _uiState.update {
                        it.copy(
                            monthlyData = monthlyStats,
                            categoryBreakdown = categoryStats,
                            yearlyOverview = yearlyOverview,
                            selectedYear = selectedYear,
                            selectedVehicleId = vehicleId,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateMonthlyStatistics(fuels: List<Fuel>, year: Int): List<MonthlyStatistic> {
        val monthlyStats = mutableListOf<MonthlyStatistic>()
        val calendar = Calendar.getInstance()
        val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        for (month in 0..11) {
            val monthFuels = fuels.filter {
                calendar.timeInMillis = it.date
                calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
            }

            if (monthFuels.isNotEmpty()) {
                val totalSpent = monthFuels.sumOf { it.totalCost }
                val totalFuel = monthFuels.sumOf { it.fuelAdded }
                val averageEconomy = monthFuels.map { it.fuelEconomy }.average()

                monthlyStats.add(
                    MonthlyStatistic(
                        month = monthNames[month],
                        year = year,
                        totalSpent = totalSpent,
                        totalFuel = totalFuel,
                        averageEconomy = averageEconomy,
                        fuelCount = monthFuels.size
                    )
                )
            }
        }

        return monthlyStats
    }

    private fun calculateCategoryStatistics(fuels: List<Fuel>): List<CategoryStatistic> {
        val categoryMap = mutableMapOf<String, CategoryStatistic>()

        fuels.groupBy { it.vehicleId }.forEach { (_, fuelList) ->
            val categoryName = "Fuel"
            val totalSpent = fuelList.sumOf { it.totalCost }
            val count = fuelList.size

            categoryMap[categoryName] = CategoryStatistic(
                categoryName = categoryName,
                totalSpent = (categoryMap[categoryName]?.totalSpent ?: 0.0) + totalSpent,
                percentage = 0.0,
                count = (categoryMap[categoryName]?.count ?: 0) + count
            )
        }

        val totalSpentAll = categoryMap.values.sumOf { it.totalSpent }

        return categoryMap.values.map { stat ->
            stat.copy(percentage = if (totalSpentAll > 0) (stat.totalSpent / totalSpentAll * 100) else 0.0)
        }.sortedByDescending { it.totalSpent }
    }

    private fun calculateYearlyOverview(fuels: List<Fuel>): YearlyOverview {
        if (fuels.isEmpty()) return YearlyOverview()

        val totalSpent = fuels.sumOf { it.totalCost }
        val totalFuel = fuels.sumOf { it.fuelAdded }
        val totalDistance = fuels.sumOf { it.trip }
        val averageEconomy = fuels.map { it.fuelEconomy }.average()

        return YearlyOverview(
            totalSpent = totalSpent,
            totalFuel = totalFuel,
            averageEconomy = averageEconomy,
            totalTrips = fuels.size,
            totalDistance = totalDistance,
            fuelStations = emptySet()

        )
    }

    fun setSelectedYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        loadStatistics(_uiState.value.selectedVehicleId, year)
    }

    fun setSelectedVehicle(vehicleId: String?) {
        _uiState.update { it.copy(selectedVehicleId = vehicleId) }
        loadStatistics(vehicleId, _uiState.value.selectedYear)
    }
}