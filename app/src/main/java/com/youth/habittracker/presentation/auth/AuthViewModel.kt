package com.youth.habittracker.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.youth.habittracker.data.model.User
import com.youth.habittracker.data.repository.AuthRepository
import com.youth.habittracker.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val loginState: StateFlow<UiState<Unit>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val registerState: StateFlow<UiState<Unit>> = _registerState.asStateFlow()

    private val _passwordResetState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val passwordResetState: StateFlow<UiState<String>> = _passwordResetState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _currentUser.value = user
            }
        }
    }

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.signInWithEmail(email, password)
                .onSuccess {
                    _loginState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _loginState.value = UiState.Error(exception.message ?: "Login failed")
                }
        }
    }

    fun signUpWithEmail(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            authRepository.signUpWithEmail(email, password)
                .onSuccess { authResult ->
                    // Create user profile in Firestore
                    val user = User(
                        userId = authResult.user?.uid ?: "",
                        email = email,
                        displayName = displayName
                    )
                    authRepository.createOrUpdateUser(user)
                        .onSuccess {
                            _registerState.value = UiState.Success(Unit)
                        }
                        .onFailure { exception ->
                            _registerState.value = UiState.Error(exception.message ?: "Registration failed")
                        }
                }
                .onFailure { exception ->
                    _registerState.value = UiState.Error(exception.message ?: "Registration failed")
                }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.signInWithGoogle(account)
                .onSuccess { authResult ->
                    // Check if user exists in Firestore, if not create
                    val userId = authResult.user?.uid ?: return@onSuccess
                    authRepository.getUser(userId)
                        .onSuccess { existingUser ->
                            if (existingUser == null) {
                                // Create new user profile
                                val user = User(
                                    userId = userId,
                                    email = authResult.user?.email ?: "",
                                    displayName = authResult.user?.displayName ?: "",
                                    profilePictureUrl = authResult.user?.photoUrl?.toString()
                                )
                                authRepository.createOrUpdateUser(user)
                            }
                        }
                    _loginState.value = UiState.Success(Unit)
                }
                .onFailure { exception ->
                    _loginState.value = UiState.Error(exception.message ?: "Google sign in failed")
                }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _passwordResetState.value = UiState.Loading
            authRepository.sendPasswordResetEmail(email)
                .onSuccess {
                    _passwordResetState.value = UiState.Success("Password reset email sent")
                }
                .onFailure { exception ->
                    _passwordResetState.value = UiState.Error(exception.message ?: "Password reset failed")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearLoginState() {
        _loginState.value = UiState.Idle
    }

    fun clearRegisterState() {
        _registerState.value = UiState.Idle
    }

    fun clearPasswordResetState() {
        _passwordResetState.value = UiState.Idle
    }
}