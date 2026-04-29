package cc.n0th1ng.tripmoney

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ReportDrawnWhen
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.data.entity.Trip
import cc.n0th1ng.tripmoney.navigation.BottomNavigation
import cc.n0th1ng.tripmoney.navigation.CustomNavigationDrawer
import cc.n0th1ng.tripmoney.navigation.Screens
import cc.n0th1ng.tripmoney.navigation.TopBar
import cc.n0th1ng.tripmoney.navigation.TopBarSettings
import cc.n0th1ng.tripmoney.screens.listexpense.ListExpenseScreen
import cc.n0th1ng.tripmoney.screens.managecategories.ManageCategoriesScreen
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

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripMoneyTheme {
                NavigationDrawer()
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun NavigationDrawer() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val tripViewModel: TripViewModel = hiltViewModel()
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val categories by expenseAndCategoryViewModel.getCategories().collectAsState(emptyList())
    val currentTripId by settingsViewModel.currentTrip.collectAsState()
    val currentTrip by tripViewModel.getTrip(currentTripId).collectAsState(Trip.DUMMY)
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination?.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var search by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(Filter()) }
    val autoOpenPref by settingsViewModel.autoOpenStartupPref.collectAsState()
    var hasHandledStartupOpen by rememberSaveable { mutableStateOf(false) }
    val shouldTriggerAutoOpen = autoOpenPref == true && !hasHandledStartupOpen
    ReportDrawnWhen { !categories.isEmpty() }
    CustomNavigationDrawer(navController, drawerState) {
        Scaffold(
            topBar = {
                if (current == Screens.SETTINGS) TopBarSettings(
                    navController
                ) else TopBar(
                    title = currentTrip?.name ?: "",
                    onDrawerClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                    isSearchable = current == Screens.LIST_EXPENSE,
                    onSearchChange = { newSearch -> search = newSearch },
                    onFilterChange = { newFilter -> filter = newFilter },
                    categories = categories,
                    filter = filter
                )
            },

            bottomBar = { BottomNavigation(navController) }) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (currentTripId == -1) Screens.TRIP_PICKER else Screens.LIST_EXPENSE,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screens.LIST_EXPENSE) {
                    ListExpenseScreen(
                        filter = filter, search = search,
                        initialAutoOpen = shouldTriggerAutoOpen,
                        onAutoOpenConsumed = { hasHandledStartupOpen = true })
                }
                composable(Screens.TRIP_PICKER) {
                    TripPickerScreen(navController)
                }
                composable(Screens.STATISTICS) {
                    StatisticsScreen()
                }
                composable(Screens.SETTINGS) {
                    SettingsScreen(navController)
                }
                composable(Screens.MANAGE_CATEGORIES) {
                    ManageCategoriesScreen()
                }
            }
        }
    }
}

data class Filter(
    val categories: List<Category> = emptyList(), val startAmount: Double = 0.0,
    val endAmount: Double = Double.MAX_VALUE
) {
    fun with(category: Category): Filter {
        return this.copy(categories = categories + category)
    }

    fun withStartAmount(amount: Double): Filter {
        return this.copy(startAmount = amount)
    }

    fun withEndAmount(amount: Double): Filter {
        return this.copy(endAmount = amount)
    }

    fun without(category: Category): Filter {
        return this.copy(categories = categories - category)
    }

    fun isDefault(): Boolean {
        return this.categories.isEmpty() && startAmount == 0.0 && endAmount == Double.MAX_VALUE
    }
}