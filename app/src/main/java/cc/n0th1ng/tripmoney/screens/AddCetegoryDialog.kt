package cc.n0th1ng.tripmoney.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import cc.n0th1ng.tripmoney.data.entity.Category
import cc.n0th1ng.tripmoney.utils.Icons
import cc.n0th1ng.tripmoney.utils.colors

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(Icons.entries[0]) }
    var color by remember { mutableStateOf(colors[0]) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Add new category") }, text = {
            AlertDialogFill(
                onTextChange = { newText ->
                    name = newText
                },
                onIconChange = { newIcon -> icon = newIcon },
                onColorChange = { newColor -> color = newColor }
            )
        }, confirmButton = {
            Button(
                enabled = !name.isEmpty(),
                onClick = {
                    onSave(
                        Category(
                            name = name,
                            icon = icon,
                            color = color
                        )
                    )
                }) { Text("Save") }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                onClick = onDismiss
            ) { Text("close") }
        })
}

@Composable
fun AlertDialogFill(
    onTextChange: (String) -> Unit,
    onIconChange: (Icons) -> Unit,
    onColorChange: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var iconId by remember { mutableIntStateOf(Icons.entries[0].resource) }
    var colorHex by remember { mutableStateOf(colors[0]) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                modifier = Modifier.size(30.dp),
                painter = painterResource(iconId), contentDescription = null,
                tint = Color(colorHex.toColorInt())
            )
            OutlinedTextField(label = { Text("Name") }, value = text, onValueChange = { newText ->
                text = newText
                onTextChange(text)
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
                            iconId = icon.resource
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
                            colorHex = color
                            onColorChange(colorHex)
                        })
                        .size(30.dp)
                        .aspectRatio(1f)
                        .background(Color(color.toColorInt()))
                ) {}
            }
        }
    }
}