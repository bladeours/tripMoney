package cc.n0th1ng.tripmoney.screens.trippicker

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.navigation.Screens
import cc.n0th1ng.tripmoney.screens.addexpense.AddExpenseBottomSheet
import cc.n0th1ng.tripmoney.screens.listexpense.DeleteConfirmationDialog
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun TripPickerScreen(
    navController: NavController
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    var showBottomSheet by remember { mutableStateOf(false) }
    val trips: LazyPagingItems<Trip> = tripViewModel.getTrips().collectAsLazyPagingItems()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    var tripToEdit by remember { mutableStateOf<Trip?>(null) }
    Scaffold(floatingActionButtonPosition = FabPosition.EndOverlay, floatingActionButton = {
        FloatingActionButton(
            onClick = { showBottomSheet = true }) {
            Icon(Icons.Filled.Add, stringResource(string.add_trip))
        }
    }) { paddingValues ->
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
                    SwipeToDeleteTripCard(
                        trip, onDelete = {
                        tripViewModel.delete(trip)
                    }, onClick = {
                        settingsViewModel.setCurrentTrip(trip.id)
                        navController.navigate(Screens.LIST_EXPENSE)
                    }, isSelected = currentTripId == trip.id,
                        onLongClick = { trip ->
                            tripToEdit = trip
                            showBottomSheet = true
                        })
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        if (showBottomSheet) {
            AddTripBottomSheet(
                onDismiss = {
                    showBottomSheet = false
                    tripToEdit = null
                },
                onSave = { trip ->
                    tripViewModel.save(trip)
                    showBottomSheet = false
                    tripToEdit = null
                },
                tripToEdit = tripToEdit,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeToDeleteTripCard(
    trip: Trip, onDelete: (Trip) -> Unit, onClick: (Trip) -> Unit, isSelected: Boolean,
    onLongClick: (Trip) -> Unit
) {
    var dismissed by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    if (!dismissed) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                    showDialog = true
                    false
                } else {
                    false
                }
            })
        if (showDialog) {
            DeleteConfirmationDialog(onConfirm = {
                showDialog = false
                dismissed = true
                onDelete(trip)
            }, onCancel = { showDialog = false })
        }

        SwipeToDismissBox(
            modifier = Modifier.alpha(if (isSelected) 1.0f else 0.7f),
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                Box(
                    Modifier
                        .clip(CardDefaults.elevatedShape)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.onError)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(string.delete))
                }
            }) {
            TripCard(trip, isSelected, onClick = onClick, onLongClick = onLongClick)
        }
    }
}


@Composable
fun TripCard(
    trip: Trip,
    isSelected: Boolean,
    onClick: (Trip) -> Unit,
    onLongClick: (Trip) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }
        ),
        modifier = Modifier
            .height(100.dp)
            .combinedClickable(enabled = true, onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick(trip)
            }, onClick = { onClick(trip) }),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 7.dp else 0.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
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