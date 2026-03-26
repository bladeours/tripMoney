package cc.n0th1ng.tripmoney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cc.n0th1ng.tripmoney.data.entity.Expense
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(private val repository: TripRepository) : ViewModel() {

    fun getTrips(): Flow<PagingData<Trip>> = repository.getTrips().cachedIn(viewModelScope)

    fun getTrip(tripId: Int): Flow<Trip?> = repository.getTrip(tripId)

    fun delete(trip: Trip) {
        viewModelScope.launch {
            repository.delete(trip)
        }
    }

    fun save(trip: Trip) {
        viewModelScope.launch {
            repository.save(trip)
        }
    }

}