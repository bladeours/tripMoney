package cc.n0th1ng.tripmoney.screens.trippicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import cc.n0th1ng.tripmoney.navigation.Screens
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel

@Composable
fun TripPickerScreen(
    navController: NavController
) {

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val trips: LazyPagingItems<Trip> = tripViewModel.getTrips().collectAsLazyPagingItems()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 15.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {

        items(trips.itemCount, trips.itemKey { it.id }) { i ->
            Spacer(Modifier.height(10.dp))
            val trip = trips[i]
            if (trip != null) {
                TripCard(trip, currentTripId == trip.id, onClick = {
                    settingsViewModel.setCurrentTrip(trip.id)
                    navController.navigate(Screens.LIST_EXPENSE)
                })
            }
            Spacer(Modifier.height(10.dp))
        }
    }


}

@Composable
fun TripCard(trip: Trip, isSelected: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .height(100.dp)
            .clickable(true, onClick = onClick)
            .alpha(if (isSelected) 1.0f else 0.7f),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 7.dp else 0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(fontSize = 25.sp, fontWeight = FontWeight.SemiBold, text = trip.name)
                Text(trip.startDate)
            }
            Text(
                trip.currency.uppercase(),
                modifier = Modifier.padding(20.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

    }
}