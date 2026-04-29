package cc.n0th1ng.tripmoney.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cc.n0th1ng.tripmoney.data.dao.CategoryDao
import cc.n0th1ng.tripmoney.data.dao.ExchangeRateDao
import cc.n0th1ng.tripmoney.data.dao.ExpenseDao
import cc.n0th1ng.tripmoney.data.dao.TripDao
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.ExchangeRate
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.utils.Currencies
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.colors
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Singleton
import kotlin.random.Random

@Database(
    entities = [Trip::class, Expense::class, Category::class, ExchangeRate::class],
    version = 1
)
@TypeConverters(Converters::class)
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
//        val db: TripDatabase = Room.inMemoryDatabaseBuilder(
        val db: TripDatabase = Room.databaseBuilder(
            name = "tripmoney_db",
            context = context,
            klass = TripDatabase::class.java,
        )
//            .allowMainThreadQueries() // TODO Remove in production!
            .fallbackToDestructiveMigration() // TODO Handle schema changes during dev
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            DatabasePrepopulator(
                tripDao = db.tripDao(),
                categoryDao = db.categoryDao(),
                expenseDao = db.expenseDao()
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

        tripDao.insert(
            Trip(
                name = "Włochy",
                startDate = LocalDate.parse("2026-03-01"),
                endDate = LocalDate.parse("2026-03-15"),
                currency = "PLN"
            )
        )
        tripDao.insert(
            Trip(
                name = "Szwajcaria",
                startDate = LocalDate.parse("2025-03-01"),
                endDate = LocalDate.parse("2025-03-15"),
                currency = "EUR"
            )
        )
        tripDao.insert(
            Trip(
                name = "Portugalia",
                startDate = LocalDate.parse("2025-03-01"),
                endDate = LocalDate.parse("2025-03-15"),
                currency = "USD"
            )
        )
        for (category in sampleCategories) {
            categoryDao.insert(category)
        }
        for (expense in sampleExpenses) {
            expenseDao.insert(expense)
        }


    }

    val sampleCategories = listOf(
        Category(
            name = "Hotel",
            icon = Icons.HOTEL,
            color = colors.random()
        ),
        Category(
            name = "Jedzenie",
            icon = Icons.RESTAURANT,
            color = colors.random()
        ),
        Category(
            name = "Transport",
            icon = Icons.FLIGHT,
            color = colors.random()
        ),
        Category(
            name = "Rozrywka",
            icon = Icons.ATTRACTION,
            color = colors.random()
        ),
        Category(
            name = "Zakupy",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy1",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy2",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy3",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy4",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy5",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy6",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy7",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy8",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
        Category(
            name = "Zakupy9",
            icon = Icons.GROCERIES,
            color = colors.random()
        ),
    )

    @RequiresApi(Build.VERSION_CODES.O)
    val sampleExpenses = (0..150).map { i ->

        val datetime = if (i > 4) {
            val now = LocalDateTime.now()
            val min = now.minusDays(10).toInstant(ZoneOffset.UTC).toEpochMilli()
            val max = now.toInstant(ZoneOffset.UTC).toEpochMilli()
            val randomMillis = Random.nextLong(min, max)
            LocalDateTime.ofInstant(Instant.ofEpochMilli(randomMillis), ZoneOffset.UTC)
        } else {
            LocalDateTime.now()
        }


        val expense = Expense(
            categoryId = Random.nextInt(1, sampleCategories.size),
            tripId = 1,
            amount = Random.nextDouble(0.1, 300.0),
            currency = Currencies.entries.random().name,
            note = if (i % 3 == 0) "Some note" else "",
            datetime = datetime,
            rate = if (Random.nextBoolean()) Random.nextDouble(
                0.1,
                5.0
            ) else 1.0
        )
        expense
    }
}