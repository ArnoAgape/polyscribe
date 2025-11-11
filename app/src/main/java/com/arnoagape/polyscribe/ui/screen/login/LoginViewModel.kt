package com.arnoagape.polyscribe.ui.screen.login

import androidx.lifecycle.ViewModel
import com.arnoagape.polyscribe.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

}