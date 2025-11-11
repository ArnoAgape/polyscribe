package com.arnoagape.polyscribe.ui.screen.home

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.arnoagape.polyscribe.domain.model.File
import com.arnoagape.polyscribe.ui.components.BottomBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onFileClick: (File) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onFABClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
) {

}

