package dm.com.carlog.data.expense

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseCategoryRepository @Inject constructor(
    private val expenseCategoryDao: ExpenseCategoryDao
) {
    suspend fun insert(category: ExpenseCategory): Long {
        return expenseCategoryDao.insert(category)
    }

    suspend fun update(category: ExpenseCategory) {
        expenseCategoryDao.update(category)
    }

    fun getAll(): Flow<List<ExpenseCategory>> = expenseCategoryDao.getAll()

    suspend fun getById(id: String): ExpenseCategory? = expenseCategoryDao.getById(id)

    fun getDefaultCategories(): Flow<List<ExpenseCategory>> = expenseCategoryDao.getDefaultCategories()

    suspend fun delete(id: String) {
        expenseCategoryDao.delete(id)
    }

    suspend fun seedDefaultCategories() {
        val defaultCategories = listOf(
            ExpenseCategory(
                id = "1",
                name = "Fuel",
                icon = "⛽",
                color = "#FF9800",
                isDefault = true
            ),
            ExpenseCategory(
                id = "2",
                name = "Insurance",
                icon = "📋",
                color = "#2196F3",
                isDefault = true
            ),
            ExpenseCategory(
                id = "3",
                name = "Maintenance",
                icon = "🔧",
                color = "#4CAF50",
                isDefault = true
            ),
            ExpenseCategory(
                id = "4",
                name = "Repair",
                icon = "🚗",
                color = "#F44336",
                isDefault = true
            ),
            ExpenseCategory(
                id = "5",
                name = "Parking/Tolls",
                icon = "🅿️",
                color = "#9C27B0",
                isDefault = true
            ),
            ExpenseCategory(
                id = "6",
                name = "Washing",
                icon = "🧼",
                color = "#00BCD4",
                isDefault = true
            )
        )

        expenseCategoryDao.insertAll(defaultCategories)
    }
}