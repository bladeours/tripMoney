package cc.n0th1ng.tripmoney.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cc.n0th1ng.tripmoney.data.dao.CategoryDao
import cc.n0th1ng.tripmoney.data.dao.ExchangeRateDao
import cc.n0th1ng.tripmoney.data.dao.ExpenseDao
import cc.n0th1ng.tripmoney.data.dao.TripDao
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.ExchangeRate
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.utils.Icons
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Database(entities = [Trip::class, Expense::class, Category::class, ExchangeRate::class], version = 1)
abstract class TripDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun exchangeRateDao(): ExchangeRateDao
}


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideTripDatabase(
        @ApplicationContext context: Context
    ): TripDatabase {

        val db: TripDatabase = Room.inMemoryDatabaseBuilder(
            context, TripDatabase::class.java
        ).allowMainThreadQueries().build()

        CoroutineScope(Dispatchers.IO).launch {
            DatabasePrepopulator(
                tripDao = db.tripDao(), categoryDao = db.categoryDao(), expenseDao = db.expenseDao()
            ).prepopulate()
        }
        return db
    }

    @Provides
    @Singleton
    fun provideExpenseDao(database: TripDatabase): ExpenseDao {
        return database.expenseDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(database: TripDatabase): TripDao {
        return database.tripDao()
    }

    @Provides
    @Singleton
    fun provideCategoryDao(database: TripDatabase): CategoryDao {
        return database.categoryDao()
    }

    @Provides
    @Singleton
    fun provideExchangeRateDao(database: TripDatabase): ExchangeRateDao {
        return database.exchangeRateDao()
    }
}


private class DatabasePrepopulator(
    private val tripDao: TripDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun prepopulate() {
        tripDao.insert(Trip(name = "Włochy", startDate = "2025-01-01", currency = "PLN"))
        tripDao.insert(Trip(name = "Szwajcaria", startDate = "2025-03-01", currency = "EUR"))
        tripDao.insert(Trip(name = "Portugalia", startDate = "2026-03-01", currency = "USD"))
        categoryDao.insert(Category(name = "Hotel", icon = Icons.HOTEL, color = "#B3E5FC"))
        categoryDao.insert(Category(name = "Jedzenie", icon = Icons.RESTAURANT, color = "#C8E6C9"))
        categoryDao.insert(Category(name = "Transport", icon = Icons.FLIGHT, color = "#FFCDD2"))
        categoryDao.insert(Category(name = "Rozrywka", icon = Icons.ATTRACTION, color = "#FFF9C4"))
        categoryDao.insert(Category(name = "Zakupy", icon = Icons.GROCERIES, color = "#E1BEE7"))
        categoryDao.insert(Category(name = "Zakupy1", icon = Icons.GROCERIES, color = "#D7CCC8"))
        categoryDao.insert(Category(name = "Zakupy2", icon = Icons.GROCERIES, color = "#BBDEFB"))
        categoryDao.insert(Category(name = "Zakupy3", icon = Icons.GROCERIES, color = "#D1C4E9"))
        categoryDao.insert(Category(name = "Zakupy4", icon = Icons.GROCERIES, color = "#DCEDC8"))
        categoryDao.insert(Category(name = "Zakupy5", icon = Icons.GROCERIES, color = "#F0F4C3"))
        categoryDao.insert(Category(name = "Zakupy6", icon = Icons.GROCERIES, color = "#FFE0B2"))
        categoryDao.insert(Category(name = "Zakupy7", icon = Icons.GROCERIES, color = "#D7CCC8"))
        categoryDao.insert(Category(name = "Zakupy8", icon = Icons.GROCERIES, color = "#CFD8DC"))

        val now = LocalDateTime.now()
        expenseDao.insert(
            Expense(
                amount = 120.50,
                currency = "PLN",
                note = "Hotel overnight",
                datetime = now.minusDays(10).toString(),
                categoryId = 1,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 45.75,
                currency = "PLN",
                note = "Dinner",
                datetime = now.minusDays(9).toString(),
                categoryId = 2,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 15.20,
                currency = "PLN",
                note = "Bus ticket",
                datetime = now.minusDays(8).toString(),
                categoryId = 3,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 89.99,
                currency = "PLN",
                note = "Concert tickets",
                datetime = now.minusDays(7).toString(),
                categoryId = 4,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 32.50,
                currency = "PLN",
                note = "Souvenirs",
                datetime = now.minusDays(6).toString(),
                categoryId = 5,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 180.00,
                currency = "PLN",
                note = "Hotel 3 nights",
                datetime = now.minusDays(5).toString(),
                categoryId = 1,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 67.30,
                currency = "PLN",
                note = "Lunch",
                datetime = now.minusDays(4).toString(),
                categoryId = 2,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 22.00,
                currency = "PLN",
                note = "Train ticket",
                datetime = now.minusDays(3).toString(),
                categoryId = 3,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 55.00,
                currency = "PLN",
                note = "Museum entry",
                datetime = now.minusDays(2).toString(),
                categoryId = 4,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 12.99,
                currency = "PLN",
                note = "Snacks",
                datetime = now.minusDays(1).toString(),
                categoryId = 2,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 210.00,
                currency = "PLN",
                note = "Hotel 5 nights",
                datetime = now.toString(),
                categoryId = 1,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 95.50,
                currency = "EUR",
                note = "Dinner for two",
                datetime = now.minusHours(12).toString(),
                categoryId = 2,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 30.00,
                currency = "EUR",
                note = "Taxi",
                datetime = now.minusHours(6).toString(),
                categoryId = 3,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 40.00,
                currency = "USD",
                note = "Gifts",
                datetime = now.minusHours(3).toString(),
                categoryId = 5,
                tripId = 1
            )
        )
        expenseDao.insert(
            Expense(
                amount = 75.00,
                currency = "PLN",
                note = "Sightseeing tour",
                datetime = now.minusHours(1).toString(),
                categoryId = 4,
                tripId = 1
            )
        )
    }
}
