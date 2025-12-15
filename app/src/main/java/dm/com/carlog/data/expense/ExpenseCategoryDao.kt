package dm.com.carlog.data.expense

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ExpenseCategory): Long

    @Update
    suspend fun update(category: ExpenseCategory)

    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun getAll(): Flow<List<ExpenseCategory>>

    @Query("SELECT * FROM expense_categories WHERE id = :id")
    suspend fun getById(id: String): ExpenseCategory?

    @Query("SELECT * FROM expense_categories WHERE is_default = 1")
    fun getDefaultCategories(): Flow<List<ExpenseCategory>>

    @Query("DELETE FROM expense_categories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM expense_categories")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<ExpenseCategory>)
}