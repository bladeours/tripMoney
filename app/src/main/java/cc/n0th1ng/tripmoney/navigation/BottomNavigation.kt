package cc.n0th1ng.tripmoney.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.composables.icons.materialsymbols.outlined.R

@Composable
fun BottomNavigation(navController: NavController) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val current = navBackStack?.destination?.route

    NavigationBar {
        NavigationBarItem(
            selected = current?.contains(Screens.TRIP_PICKER) == true,
            onClick = { navController.navigate(Screens.TRIP_PICKER) },
            icon = {
                Icon(
                    painter = painterResource(
                        R.drawable.materialsymbols_ic_luggage_outlined
                    ), "trip picker"
                )
            }
        )
        NavigationBarItem(
            selected = current?.contains(Screens.LIST_EXPENSE) == true,
            onClick = { navController.navigate(Screens.LIST_EXPENSE) },
            icon = {
                Icon(
                    painter = painterResource(
                        R.drawable.materialsymbols_ic_list_outlined,
                    ),
                    "list screen"
                )
            }
        )

        NavigationBarItem(
            selected = current?.contains(Screens.STATISTICS) == true,
            onClick = { navController.navigate(Screens.STATISTICS) },
            icon = {
                Icon(
                    painter = painterResource(
                        R.drawable.materialsymbols_ic_pie_chart_outlined,
                    ),
                    null
                )
            }
        )
    }
}