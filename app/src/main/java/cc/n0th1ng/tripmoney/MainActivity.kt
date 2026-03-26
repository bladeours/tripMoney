package cc.n0th1ng.tripmoney

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.navigation.BottomNavigation
import cc.n0th1ng.tripmoney.navigation.CustomNavigationDrawer
import cc.n0th1ng.tripmoney.navigation.Screens
import cc.n0th1ng.tripmoney.navigation.TopBar
import cc.n0th1ng.tripmoney.navigation.TopBarSettings
import cc.n0th1ng.tripmoney.screens.listexpense.ListExpenseScreen
import cc.n0th1ng.tripmoney.screens.settings.SettingsScreen
import cc.n0th1ng.tripmoney.screens.statistics.StatisticsScreen
import cc.n0th1ng.tripmoney.screens.trippicker.TripPickerScreen
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import cc.n0th1ng.tripmoney.viewmodel.SettingsViewModel
import cc.n0th1ng.tripmoney.viewmodel.TripViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripMoneyTheme {
                val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
                expenseAndCategoryViewModel.clearOldRates()
                NavigationDrawer()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationDrawer() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CustomNavigationDrawer(navController, drawerState) {
        Scaffold(
            topBar = {
                if (current == Screens.SETTINGS) TopBarSettings(
                    navController
                ) else TopBar(
                    title = currentTrip?.name ?: "",
                    onClick = {
                    scope.launch {
                        if (drawerState.isClosed) {
                            drawerState.open()
                        } else {
                            drawerState.close()
                        }
                    }
                })
            },

            bottomBar = { BottomNavigation(navController) }) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if(currentTripId == -1) Screens.TRIP_PICKER else Screens.LIST_EXPENSE,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screens.LIST_EXPENSE) {
                    ListExpenseScreen()
                }
                composable(Screens.TRIP_PICKER) {
                    TripPickerScreen(navController)
                }
                composable(Screens.STATISTICS) {
                    StatisticsScreen()
                }
                composable(Screens.SETTINGS) {
                    SettingsScreen()
                }
            }
        }
    }

}