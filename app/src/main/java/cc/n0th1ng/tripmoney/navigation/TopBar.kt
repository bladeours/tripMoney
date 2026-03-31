package cc.n0th1ng.tripmoney.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import com.composables.icons.materialsymbols.outlined.R.drawable
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onDrawerClick: () -> Unit,
    title: String = "",
    isSearchable: Boolean = false,
    onFilterChange: (String) -> Unit
) {
    var isSearch by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        title = {
            if (isSearch && isSearchable) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                OutlinedTextField(
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(0.9f).focusRequester(focusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent
                    ),
                    value = value,
                    onValueChange = { newText ->
                        value = newText
                    },
                    singleLine = true,
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.clickable(onClick = {
                                isSearch = false
                                value = ""
                                onFilterChange("")
                            }),
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                )
                LaunchedEffect(key1 = value) {
                    delay(1000)
                    onFilterChange(value)
                }
            } else {
                Text(title)
            }
        },
        navigationIcon = {
            IconButton(onClick = { onDrawerClick() }) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        },
        actions = {
            if (!isSearch && isSearchable) {
                Row(
                    modifier = Modifier.padding(end = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(drawable.materialsymbols_ic_filter_alt_outlined),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {})
                    )
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(drawable.materialsymbols_ic_search_outlined),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            isSearch = true
                        })
                    )

                }
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

@AllPreviews
@Composable
fun PreviewTopBar() {
    TripMoneyTheme {
        TopBar(
            onDrawerClick = {},
            title = "Essa",
            onFilterChange = {}
        )
    }
}
