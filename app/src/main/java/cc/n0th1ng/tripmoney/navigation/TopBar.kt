package cc.n0th1ng.tripmoney.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import cc.n0th1ng.tripmoney.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onClick: () -> Unit, title: String = "") {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = { onClick() }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarSettings(navController: NavHostController) {

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}