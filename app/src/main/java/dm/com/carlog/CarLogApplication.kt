package dm.com.carlog

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltAndroidApp
class CarLogApplication : Application() {

    @Inject
    lateinit var expenseCategoryRepository: dm.com.carlog.data.expense.ExpenseCategoryRepository

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                expenseCategoryRepository.seedDefaultCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}