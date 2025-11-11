package com.arnoagape.polyscribe.ui.screen.home

import androidx.lifecycle.ViewModel
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

}