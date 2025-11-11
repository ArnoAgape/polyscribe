package com.arnoagape.polyscribe.ui.screen.send

import androidx.lifecycle.ViewModel
import com.arnoagape.polyscribe.data.repository.FileRepository
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

}