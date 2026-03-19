package cc.n0th1ng.tripmoney.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.composables.icons.materialsymbols.outlined.R
import kotlinx.coroutines.launch

@Composable
fun CustomNavigationDrawer(
    navController: NavController, drawerState: DrawerState, content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState, drawerContent = {
            ModalDrawerSheet {
                Text("Trip Money", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                NavigationDrawerItem(
                    label = { Text(text = "Pick trip") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screens.TRIP_PICKER)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                R.drawable.materialsymbols_ic_luggage_outlined,
                            ), null
                        )
                    })
                NavigationDrawerItem(
                    label = { Text(text = "List of expenses") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screens.LIST_EXPENSE)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                R.drawable.materialsymbols_ic_list_outlined,
                            ), null
                        )
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Statistics") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screens.STATISTICS)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    icon = {
                        Icon(
                            painter = painterResource(
                                R.drawable.materialsymbols_ic_pie_chart_outlined,
                            ), null
                        )
                    })
                NavigationDrawerItem(
                    label = { Text(text = "Settings") },
                    selected = false,
                    onClick = {
                        navController.navigate(Screens.SETTINGS)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "settings") }
                )
            }
        }) { content() }
}

object Screens {
    const val LIST_EXPENSE = "list_expense"
    const val TRIP_PICKER = "trip_picker"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
}