package cc.n0th1ng.tripmoney.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cc.n0th1ng.tripmoney.data.dao.TripDao
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class TripRepository @Inject constructor(
    private val tripDao: TripDao,
    private val expenseRepository: ExpenseRepository
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun save(trip: Trip) {
        tripDao.insert(trip)
    }

    fun getTrips(): Flow<PagingData<Trip>> {
        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { tripDao.tripsPaged() }
        ).flow
    }

    fun getTrip(tripId: Int): Flow<Trip?> {
        return tripDao.trip(tripId)
    }

    @WorkerThread
    suspend fun delete(trip: Trip) {
        tripDao.delete(trip)
    }
}