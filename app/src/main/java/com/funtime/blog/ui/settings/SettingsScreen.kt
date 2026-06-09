package com.funtime.blog.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("閱讀設定") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 字體大小
            Text("字體大小", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(85 to "小", 100 to "中", 120 to "大").forEach { (size, label) ->
                    FilterChip(
                        selected = uiState.fontSize == size,
                        onClick = { viewModel.setFontSize(size) },
                        label = { Text(label) }
                    )
                }
            }

            HorizontalDivider()

            // 深色模式
            Text("主題", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "跟隨系統", "light" to "淺色", "dark" to "深色").forEach { (mode, label) ->
                    FilterChip(
                        selected = uiState.darkMode == mode,
                        onClick = { viewModel.setDarkMode(mode) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}
