package cc.n0th1ng.tripmoney.screens.managecategories

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import cc.n0th1ng.tripmoney.R.string
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.screens.AddCategoryDialog
import cc.n0th1ng.tripmoney.screens.addexpense.categoriesToPreview
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.colors
import cc.n0th1ng.tripmoney.viewmodel.ExpenseAndCategoryViewModel
import com.composables.icons.materialsymbols.outlined.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.emptyList

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ManageCategoriesScreen() {
    val expenseAndCategoryViewModel: ExpenseAndCategoryViewModel = hiltViewModel()
    val categories by expenseAndCategoryViewModel.getCategories().collectAsState(emptyList())
    val archivedCategories by expenseAndCategoryViewModel.getArchivedCategories()
        .collectAsState(emptyList())
    ManageCategoriesScreen(
        categories = categories,
        archivedCategories = archivedCategories,
        onSaveCategory = { expenseAndCategoryViewModel.save(it) },
        onDeleteCategory = {
            expenseAndCategoryViewModel.delete(it)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ManageCategoriesScreen(
    categories: List<Category>,
    archivedCategories: List<Category>,
    onSaveCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
) {

    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Category?>(null) }
    var itemToArchive by remember { mutableStateOf<Category?>(null) }

    Scaffold(floatingActionButtonPosition = FabPosition.EndOverlay, floatingActionButton = {
        ExtendedFloatingActionButton(
            onClick = { showAddCategoryDialog = true },
            icon = { Icon(Icons.Filled.Add, stringResource(string.add_new)) },
            text = { Text(text = stringResource(string.add_new)) },
        )
    })
    {
        LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            items(categories, key = { it.id }) { category ->
                SwipeToDeleteExpenseCard(
                    category = category,
                    onDelete = { itemToArchive = category },
                    onClick = {
                        categoryToEdit = category
                        showAddCategoryDialog = true
                    }
                )
                Spacer(Modifier.height(10.dp))
            }

            if (archivedCategories.isNotEmpty()) {
                item {
                    CustomDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            items(archivedCategories, key = { it.id }) { archivedCategory ->
                SwipeToDeleteExpenseCard(
                    category = archivedCategory,
                    onDelete = { itemToDelete = archivedCategory },
                    onClick = {
                        categoryToEdit = archivedCategory
                        showAddCategoryDialog = true
                    },
                    isArchived = true
                )
                Spacer(Modifier.height(10.dp))
            }

        }
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = {
                    showAddCategoryDialog = false
                }, onSave = { category ->
                    onSaveCategory(category)
                    showAddCategoryDialog = false
                },
                categoryToEdit = categoryToEdit
            )
        }


    }

    if (itemToDelete != null) {
        DeleteConfirmationDialog(
            bodyText = stringResource(string.delete_category_info),
            onConfirm = {
                onDeleteCategory(itemToDelete!!)
                itemToDelete = null
            },
            onCancel = {
                itemToDelete = null
            }
        )
    }

    if (itemToArchive != null) {
        DeleteConfirmationDialog(
            title = stringResource(string.you_want_archive),
            buttonText = stringResource(string.archive),
            bodyText = stringResource(string.archive_category_info),
            onConfirm = {
                onSaveCategory(itemToArchive!!.copy(archived = true))
                itemToArchive = null
            },
            onCancel = {
                itemToArchive = null
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CustomDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Absolute.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            "Archived",
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .background(Color.White.copy(alpha = 0f)),
            style = MaterialTheme.typography.titleMedium
        )
        HorizontalDivider(modifier = Modifier.weight(1f))

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SwipeToDeleteExpenseCard(
    category: Category,
    onDelete: (Category) -> Unit,
    onClick: (Category) -> Unit,
    isArchived: Boolean = false
) {

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete(category)
                false
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
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
                Icon(
                    painter = painterResource(
                        if (isArchived) R.drawable.materialsymbols_ic_delete_outlined
                        else R.drawable.materialsymbols_ic_archive_outlined
                    ),
                    contentDescription = stringResource(string.delete)
                )
            }
        }
    ) {
        CategoryCard(
            category = category,
            onClick = onClick,
            isArchived = isArchived
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteConfirmationDialog(
    title: String = stringResource(string.delete_confirmation),
    buttonText: String = stringResource(string.delete),
    bodyText: String = "",
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = { onCancel() }
    ) {
        Column(
            Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(24.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier
                        .padding(end = 10.dp)
                ){
                    Text(text = stringResource(string.cancel),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondary)
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors().copy(containerColor = MaterialTheme.colorScheme.error),
                ){
                    Text(text = buttonText,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CategoryCard(
    category: Category,
    onClick: (Category) -> Unit,
    isArchived: Boolean = false
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors()
            .copy(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(70.dp)
            .combinedClickable(
                enabled = true,
                onClick = { onClick(category) },
                onLongClick = { onClick(category) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isArchived) 0.6f else 1f)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                Icon(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surfaceDim,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(10.dp),
                    painter = painterResource(category.icon.resource),
                    contentDescription = "Category",
                    tint = Color(category.color.toColorInt())
                )

                Column()
                {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewManageCategoriesScreen() {
    TripMoneyTheme {
        ManageCategoriesScreen(categories = categoriesToPreview.subList(0,2), categoriesToPreview.subList(3,5), {}, {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewAddCategoryDialog() {
    TripMoneyTheme {
        AddCategoryDialog(
            onDismiss = {},
            onSave = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewEditCategoryDialog() {
    TripMoneyTheme {
        AddCategoryDialog(
            onDismiss = {},
            onSave = {},
            categoryToEdit = Category(
                0, "Hotel",
                icon = cc.n0th1ng.tripmoney.utils.Icons.entries.random(),
                color = colors.random(),
                archived = false
            )
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@AllPreviews
@Composable
fun PreviewDeleteConfirmationDialog() {
    TripMoneyTheme {
        DeleteConfirmationDialog(
            onConfirm = {},
            onCancel = {},
            bodyText = "Your all expenses with category Hotel will be removed.",
            title = "Do you want to delete?",
            buttonText = "Delete"
        )
    }
}

