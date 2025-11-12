package com.arnoagape.polyscribe.ui.screen.login

import androidx.lifecycle.ViewModel
import com.arnoagape.polyscribe.data.repository.UserRepository
import com.arnoagape.polyscribe.ui.utils.NetworkUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val networkUtils: NetworkUtils
) : ViewModel() {

}