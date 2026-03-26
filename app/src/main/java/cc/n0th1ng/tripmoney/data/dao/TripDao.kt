package cc.n0th1ng.tripmoney.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import cc.n0th1ng.tripmoney.data.entity.Trip
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Upsert
    suspend fun insert(trip: Trip)

    @Query(
        """
        SELECT * FROM trip
        ORDER BY DATE(trip.start_date) DESC
    """
    )
    fun tripsPaged(): PagingSource<Int, Trip>

    @Delete
    suspend fun delete(trip: Trip)

    @Query(
        "SELECT * FROM trip where trip.id = :tripId"
    )
    fun trip(tripId: Int): Flow<Trip?>
}
