package dm.com.carlog.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dm.com.carlog.data.AppDatabase
import dm.com.carlog.data.expense.ExpenseCategoryDao
import dm.com.carlog.data.expense.ExpenseCategoryRepository
import dm.com.carlog.data.fuel.FuelDao
import dm.com.carlog.data.fuel.FuelRepository
import dm.com.carlog.data.reminder.ReminderDao
import dm.com.carlog.data.reminder.ReminderRepository
import dm.com.carlog.data.vehicle.VehicleDao
import dm.com.carlog.data.vehicle.VehicleRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideVehicleDao(database: AppDatabase): VehicleDao = database.vehicleDao()

    @Provides
    fun provideVehicleRepository(vehicleDao: VehicleDao): VehicleRepository = VehicleRepository(vehicleDao)

    @Provides
    fun provideFuelDao(database: AppDatabase): FuelDao = database.fuelDao()

    @Provides
    fun provideFuelRepository(fuelDao: FuelDao, vehicleDao: VehicleDao): FuelRepository =
        FuelRepository(vehicleDao, fuelDao)

    @Provides
    fun provideExpenseCategoryDao(database: AppDatabase): ExpenseCategoryDao = database.expenseCategoryDao()

    @Provides
    fun provideExpenseCategoryRepository(expenseCategoryDao: ExpenseCategoryDao): ExpenseCategoryRepository =
        ExpenseCategoryRepository(expenseCategoryDao)

    @Provides
    fun provideReminderDao(database: AppDatabase): ReminderDao = database.reminderDao()

    @Provides
    fun provideReminderRepository(reminderDao: ReminderDao): ReminderRepository =
        ReminderRepository(reminderDao)
}