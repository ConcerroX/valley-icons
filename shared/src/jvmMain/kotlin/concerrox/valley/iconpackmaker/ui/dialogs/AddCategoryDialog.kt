package concerrox.valley.iconpackmaker.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import concerrox.ui.component.XButton

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit, 
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss, 
        title = { Text("添加新分类") }, 
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("分类名称") },
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        }, 
        confirmButton = {
            XButton(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onConfirm(categoryName.trim())
                    }
                }, 
                enabled = categoryName.isNotBlank()
            ) {
                Text("添加")
            }
        }, 
        dismissButton = {
            XButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}