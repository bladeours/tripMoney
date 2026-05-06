package cc.n0th1ng.tripmoney.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import com.composables.icons.materialsymbols.outlined.R.drawable

@Composable
fun SearchTextOutlined(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    focusRequester: FocusRequester = FocusRequester()
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        trailingIcon = {
            if (text.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "close",
                    modifier = Modifier.clickable(true, onClick = { onTextChange("") })
                )
            } else {
                Icon(
                    painter = painterResource(drawable.materialsymbols_ic_search_outlined),
                    contentDescription = "search"
                )
            }

        }
    )
}