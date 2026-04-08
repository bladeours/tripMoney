package cc.n0th1ng.tripmoney.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cc.n0th1ng.tripmoney.Filter
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.screens.addexpense.categoriesToPreview
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
    onSearchChange: (String) -> Unit,
    filter: Filter,
    onFilterChange: (Filter) -> Unit,
    categories: List<Category>
) {
    var showSearch by remember { mutableStateOf(false) }
    var showFilter by remember { mutableStateOf(false) }
    var value by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    TopAppBar(
        title = {
            if (showSearch && isSearchable) {
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                OutlinedTextField(
                    textStyle = MaterialTheme.typography.bodyMedium,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .focusRequester(focusRequester),
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
                                showSearch = false
                                value = ""
                                onSearchChange("")
                            }),
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                )
                LaunchedEffect(key1 = value) {
                    delay(1000)
                    onSearchChange(value)
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
            if (!showSearch && isSearchable) {
                Row(
                    modifier = Modifier.padding(end = 13.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(drawable.materialsymbols_ic_filter_alt_outlined),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            showFilter = true
                        })
                    )
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(drawable.materialsymbols_ic_search_outlined),
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            showSearch = true
                        })
                    )

                }
            }

        }
    )

    if (showFilter) {
        FilterDialog(
            onDismiss = { showFilter = false },
            onSave = { newFilter ->
                onFilterChange(newFilter)
                showFilter = false
            },
            categories = categories,
            filter = filter
        )


    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onSave: (Filter) -> Unit,
    categories: List<Category>,
    filter: Filter
) {
    var filter by remember { mutableStateOf(filter) }
    var fromAmountString by remember { mutableStateOf(filter.startAmount.toString()) }
    var toAmountString by remember { mutableStateOf(filter.endAmount.toString()) }
    AlertDialog(
        onDismiss, {
            Button(
                enabled = true,
                onClick = {
                    onSave(
                        filter.withStartAmount(fromAmountString.safeToDouble())
                            .withEndAmount( toAmountString.safeToDouble())
                    )
                }) { Text(stringResource(R.string.save)) }
        }, title = { Text("Filter") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Categories")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.forEach {
                        FilterChip(selected = filter.categories.contains(it), onClick = {
                            filter = if (filter.categories.contains(it)) {
                                filter.without(it)
                            } else {
                                filter.with(it)
                            }
                        }, label = { Text(text = it.name) })
                    }
                }
                AmountTextField(label = "from", onValueChange = { newText ->
                    fromAmountString = newText
                }, value = fromAmountString)
                AmountTextField(label = "to", onValueChange = { newText ->
                    toAmountString = newText
                }, value = toAmountString)

            }
        })
}

@Composable
fun AmountTextField(label: String, onValueChange: (String) -> Unit, value: String) {
    var value by remember { mutableStateOf(value) }
    OutlinedTextField(
        label = { Text(label) },
        value = if (value == Double.MAX_VALUE.toString()) "∞" else value,
        onValueChange = { newText ->
            if (newText == Double.MAX_VALUE.toString()) {
                value = "∞"
                return@OutlinedTextField
            }
            val regex = Regex("^\\d*\\.?\\d{0,2}$")
            if (regex.matches(newText)) {
                value = newText
                onValueChange(value)
            }
        },
        placeholder = { Text("0.00") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        )
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
            onSearchChange = {},
            onFilterChange = {},
            isSearchable = true,
            categories = categoriesToPreview,
            filter = Filter()
        )
    }
}

@AllPreviews
@Composable
fun PreviewFilterDialog() {
    TripMoneyTheme {
        FilterDialog(
            onDismiss = {},
            onSave = {},
            categories = categoriesToPreview,
            filter = Filter()
        )
    }
}

private fun String.safeToDouble(): Double {
    if(this == "∞") return Double.MAX_VALUE
    if(this.isEmpty()) return 0.0
    return this.toDouble()
}