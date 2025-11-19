package com.example.takstud.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.takstud.R

@Composable
fun DeletableInfoCard(
    title: String, 
    content: String, 
    onClick: () -> Unit, 
    onDelete: () -> Unit,
    onAction: (() -> Unit)? = null,
    actionIcon: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
            if (onAction != null && actionIcon != null) {
                IconButton(onClick = onAction) { actionIcon() }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, stringResource(R.string.delete)) }
        }
    }
}
