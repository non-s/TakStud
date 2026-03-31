package com.example.takstud.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * SearchBar - Barra de busca reutilizável
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar...",
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onQueryChange("")
                        onClear?.invoke()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpar"
                    )
                }
            }
        },
        singleLine = true
    )
}

/**
 * FilterChips - Chips para filtrar
 */
@Composable
fun FilterChips(
    filters: List<String>,
    selectedFilters: List<String>,
    onFilterToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { filter ->
            val isSelected = filter in selectedFilters
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Text(
                    text = filter,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

/**
 * SearchAndFilterPanel - Painel completo com busca e filtro
 */
@Composable
fun SearchAndFilterPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    filters: List<String> = emptyList(),
    selectedFilters: List<String> = emptyList(),
    onFilterToggle: (String) -> Unit = {},
    placeholder: String = "Buscar...",
    showFilters: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = placeholder
        )

        if (showFilters && filters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FilterChips(
                filters = filters,
                selectedFilters = selectedFilters,
                onFilterToggle = onFilterToggle
            )
        }
    }
}

/**
 * SortOptions - Opções de ordenação
 */
enum class SortOption(val label: String) {
    NAME_ASC("Nome A-Z"),
    NAME_DESC("Nome Z-A"),
    DATE_NEWEST("Mais recente"),
    DATE_OLDEST("Mais antigo"),
    GRADE_HIGHEST("Nota maior"),
    GRADE_LOWEST("Nota menor")
}

/**
 * SortDropdown - Dropdown de ordenação
 */
@Composable
fun SortDropdown(
    selectedSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = modifier
    ) {
        SortOption.values().forEach { option ->
            androidx.compose.material3.DropdownMenuItem(
                text = { Text(option.label) },
                onClick = {
                    onSortChange(option)
                    expanded = false
                }
            )
        }
    }

    androidx.compose.material3.Button(
        onClick = { expanded = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text("Ordenar: ${selectedSort.label}")
    }
}
