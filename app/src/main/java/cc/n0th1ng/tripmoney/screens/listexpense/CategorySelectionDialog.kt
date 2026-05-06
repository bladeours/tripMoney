package cc.n0th1ng.tripmoney.screens.listexpense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.screens.AddCategoryDialog
import cc.n0th1ng.tripmoney.screens.addexpense.categoriesToPreview
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.SearchTextOutlined
import com.composables.icons.materialsymbols.outlined.R

@Composable
fun CategorySelectionDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    selected: Category?,
    categories: List<Category>,
    onSaveCategory: (Category) -> Unit
) {
    val listState = rememberLazyListState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(string.pick_category)) },
        text = {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) {
                if(selected != null) {
                    listState.animateScrollToItem(categories.indexOfFirst { it == selected })
                }
//                focusRequester.requestFocus()
            }
            Column {
                var search by remember { mutableStateOf("") }
                val filteredCategories = if (search.isBlank()) {
                    categories
                } else {
                    categories.filter { category ->
                        category.name.lowercase().contains(search.lowercase())
                    }
                }

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(
                        count = filteredCategories.size,
                        key = { index -> filteredCategories[index].id }) { index ->
                        val category = filteredCategories[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCategorySelected(category)
                                }
                                .padding(vertical = 0.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selected == category, onClick = {
                                    onCategorySelected(category)
                                })
                            Icon(
                                painter = painterResource(category.icon.resource),
                                contentDescription = stringResource(string.category),
                                tint = Color(category.color.toColorInt())
                            )
                            Text(
                                text = category.name, modifier = Modifier.padding(start = 8.dp),
                                color = Color(category.color.toColorInt())
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showAddCategoryDialog = true
                        }
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.materialsymbols_ic_add_outlined),
                        contentDescription = stringResource(string.category)
                    )
                    Text(
                        text = stringResource(string.add_new),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                SearchTextOutlined(
                    text = search,
                    onTextChange = { newText -> search = newText },
                    focusRequester = focusRequester
                )
            }
        },
        confirmButton = {})
    if (showAddCategoryDialog) {
        AddCategoryDialog(onDismiss = {
            showAddCategoryDialog = false
        }, onSave = { category ->
            onSaveCategory(category)
            showAddCategoryDialog = false
        })
    }
}

@AllPreviews
@Composable
fun PreviewCategorySelectionDialog() {
    TripMoneyTheme {
        CategorySelectionDialog(
            {}, {},
            categoriesToPreview.random(), categoriesToPreview, {})
    }
}