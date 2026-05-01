package cc.n0th1ng.tripmoney.screens.trippicker

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.navigation.Screens
import cc.n0th1ng.tripmoney.screens.listexpense.DeleteConfirmationDialog
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.pretty
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun TripPickerScreen(
    navController: NavController
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val tripsFlow = tripViewModel.getTrips()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()

    TripPickerScreen(
        tripsFlow = tripsFlow,
        currentTripId = currentTripId,
        onDelete = { trip -> tripViewModel.delete(trip) },
        onClick = { trip ->
            settingsViewModel.setCurrentTrip(trip.id)
            navController.navigate(Screens.LIST_EXPENSE)
        },
        onSave = { trip ->
            tripViewModel.save(trip)
        }

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun TripPickerScreen(
    tripsFlow: Flow<PagingData<Trip>>,
    currentTripId: Int,
    onDelete: (Trip) -> Unit,
    onClick: (Trip) -> Unit,
    onSave: (Trip) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val trips: LazyPagingItems<Trip> = tripsFlow.collectAsLazyPagingItems()

    var tripToEdit by remember { mutableStateOf<Trip?>(null) }
    Scaffold(floatingActionButtonPosition = FabPosition.EndOverlay, floatingActionButton = {
        FloatingActionButton(
            onClick = { showBottomSheet = true }) {
            Icon(Icons.Filled.Add, stringResource(string.add_trip))
        }
    }) { paddingValues ->
        if (trips.itemCount == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(string.no_trip_added),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        } else {
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
                            trip = trip,
                            onDelete = {
                                onDelete(trip)
                            }, onClick = {
                                onClick(trip)
                            }, isSelected = currentTripId == trip.id,
                            onLongClick = { trip ->
                                tripToEdit = trip
                                showBottomSheet = true
                            })
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }


        if (showBottomSheet) {
            AddTripBottomSheet(
                onDismiss = {
                    showBottomSheet = false
                    tripToEdit = null
                },
                onSave = { trip ->
                    onSave(trip)
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
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                Box(
                    Modifier
                        .clip(CardDefaults.elevatedShape)
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.errorContainer)
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


@RequiresApi(Build.VERSION_CODES.O)
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
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        modifier = Modifier
            .height(100.dp)
            .combinedClickable(enabled = true, onLongClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick(trip)
            }, onClick = { onClick(trip) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    text = trip.name
                )
                Text(
                    style = MaterialTheme.typography.bodySmall,
                    text = "start: " + trip.startDate.pretty() + "\nend: " + trip.endDate.pretty()
                )
            }
            Column(
                modifier = Modifier.padding(end = 20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    trip.currency.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "budget:",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    "%.2f".format(trip.budget),
                    style = MaterialTheme.typography.bodySmall,
                )

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewTripPickerScreen() {
    val tripsToPreview = listOf(
        Trip(
            1,
            name = "Włochy",
            startDate = LocalDate.parse("2026-03-01"),
            endDate = LocalDate.parse("2026-03-14"),
            currency = "PLN",
            budget = 1053.53
        ),
        Trip(
            2,
            name = "Szwajcaria",
            startDate = LocalDate.parse("2025-03-01"),
            endDate = LocalDate.parse("2025-03-11"),
            currency = "EUR"
        ),
        Trip(
            3,
            name = "Portugalia",
            startDate = LocalDate.parse("2025-03-01"),
            endDate = LocalDate.parse("2025-03-11"),
            currency = "USD"
        )
    )
    TripMoneyTheme {
        TripPickerScreen(
            tripsFlow = MutableStateFlow(PagingData.from(tripsToPreview)),
            currentTripId = 1,
            onDelete = {},
            onClick = {},
            onSave = {}
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewTripPickerScreenNoTrip() {

    TripMoneyTheme {
        TripPickerScreen(
            tripsFlow = MutableStateFlow(PagingData.from(emptyList())),
            currentTripId = 1,
            onDelete = {},
            onClick = {},
            onSave = {}
        )
    }
}