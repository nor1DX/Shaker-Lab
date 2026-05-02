package com.shakerlab.app.features.profile.view

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.shakerlab.app.domain.repository.BarRepository
import com.shakerlab.app.domain.repository.FavoritesRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val barRepository: BarRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    val favoritesCount: LiveData<Int> = favoritesRepository.getAll().map { it.size }
    val barCount: LiveData<Int> = barRepository.getIngredients().map { it.size }

    private val _currentUser = MutableLiveData<FirebaseUser?>(auth.currentUser)
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                _currentUser.value = result.user
                favoritesRepository.syncFromCloud()
                barRepository.syncFromCloud()
            } catch (_: Exception) { }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun clearFavorites() {
        viewModelScope.launch { favoritesRepository.clearAll() }
    }

    fun clearBar() {
        viewModelScope.launch { barRepository.clearAll() }
    }
}
