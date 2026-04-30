package cc.n0th1ng.tripmoney.screens

import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import cc.n0th1ng.tripmoney.R
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.theme.TripMoneyTheme
import cc.n0th1ng.tripmoney.utils.AllPreviews
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.colors

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit,
    categoryToEdit: Category? = null
) {
    var name by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var icon by remember { mutableStateOf(categoryToEdit?.icon ?: Icons.entries[0]) }
    var color by remember { mutableStateOf(categoryToEdit?.color ?: colors[0]) }
    var isArchived by remember { mutableStateOf(categoryToEdit?.archived ?: false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (categoryToEdit == null) R.string.add_new_category else R.string.edit_category)) },
        text = {
            AlertDialogFill(
                onTextChange = { newText ->
                    name = newText
                },
                onIconChange = { newIcon -> icon = newIcon },
                onColorChange = { newColor -> color = newColor },
                onArchivedChange = { newArchived ->
                    isArchived = newArchived
                },
                name = name,
                icon = icon,
                color = color,
                isArchived = isArchived
            )
        },
        confirmButton = {
            Button(
                enabled = !name.isEmpty(),
                onClick = {
                    val categoryToSave = Category(
                        name = name,
                        icon = icon,
                        color = color,
                        archived = isArchived
                    )
                    onSave(
                        if (categoryToEdit != null) categoryToSave.copy(id = categoryToEdit.id) else categoryToSave
                    )
                }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            Row() {
                Button(
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary),
                    onClick = onDismiss
                ) { Text(stringResource(R.string.cancel)) }
            }

        })
}

@Composable
fun AlertDialogFill(
    onTextChange: (String) -> Unit,
    onIconChange: (Icons) -> Unit,
    onColorChange: (String) -> Unit,
    onArchivedChange: (Boolean) -> Unit,
    name: String,
    icon: Icons,
    color: String,
    isArchived: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(icon.resource), contentDescription = null,
                tint = Color(color.toColorInt())
            )
            OutlinedTextField(label = { Text("Name") }, value = name, onValueChange = { newText ->
                onTextChange(newText)
            })
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            )
        ) {
            Icons.entries.forEach { icon ->
                Icon(
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = {
                            onIconChange(icon)
                        }),
                    painter = painterResource(icon.resource),
                    contentDescription = null,

                    )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            )
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .clickable(onClick = {
                            onColorChange(color)
                        })
                        .size(30.dp)
                        .aspectRatio(1f)
                        .background(Color(color.toColorInt()))
                ) {}
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Switch(
                checked = isArchived,
                onCheckedChange = onArchivedChange
            )
            Text(
                text = "Archived",
                style = MaterialTheme.typography.titleMedium
            )

        }

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
                archived = true
            )
        )
    }
}