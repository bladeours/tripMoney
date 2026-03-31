package cc.n0th1ng.tripmoney.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.data.repository.ExpenseRepository
import cc.n0th1ng.tripmoney.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val repository: TripRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val _isRecalculating = MutableStateFlow(false)
    val isRecalculating: StateFlow<Boolean> = _isRecalculating
    fun getTrips(): Flow<PagingData<Trip>> = repository.getTrips().cachedIn(viewModelScope)

    fun getTrip(tripId: Int): Flow<Trip?> = repository.getTrip(tripId)

    fun delete(trip: Trip) {
        viewModelScope.launch {
            repository.delete(trip)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(trip: Trip) {
        viewModelScope.launch {
            repository.save(trip)
            _isRecalculating.value = true
            withContext(Dispatchers.IO) {
                expenseRepository.recalculateTripExpenses(trip.id)
            }
            _isRecalculating.value = false
        }
    }

}