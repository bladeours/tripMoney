package cc.n0th1ng.tripmoney.data.repository

import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import cc.n0th1ng.tripmoney.data.dao.TripDao
import cc.n0th1ng.tripmoney.data.entity.Trip
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TripRepository @Inject constructor(private val tripDao: TripDao) {

    @WorkerThread
    suspend fun save(trip: Trip) {
        tripDao.insert(trip)
    }

    fun getTrips(): Flow<PagingData<Trip>> {
        return Pager(
            config = PagingConfig(pageSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { tripDao.tripsPaged() }
        ).flow
    }

    @WorkerThread
    suspend fun delete(trip: Trip) {
        tripDao.delete(trip)
    }
}