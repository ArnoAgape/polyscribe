package com.arnoagape.polyscribe.ui.screen.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    userRepository: UserRepository,
    savedStateHandle: SavedStateHandle,
    private val networkUtils: NetworkUtils
) :
    ViewModel() {

}