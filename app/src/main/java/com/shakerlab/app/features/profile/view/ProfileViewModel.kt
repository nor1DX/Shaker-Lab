@file:Suppress("DEPRECATION")
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
import com.shakerlab.app.domain.usecase.bar.ClearBarUseCase
import com.shakerlab.app.domain.usecase.bar.GetBarIngredientsUseCase
import com.shakerlab.app.domain.usecase.bar.SyncBarUseCase
import com.shakerlab.app.domain.usecase.favorites.ClearFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.GetFavoritesUseCase
import com.shakerlab.app.domain.usecase.favorites.SyncFavoritesUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    getFavoritesUseCase: GetFavoritesUseCase,
    getBarIngredientsUseCase: GetBarIngredientsUseCase,
    private val syncFavoritesUseCase: SyncFavoritesUseCase,
    private val syncBarUseCase: SyncBarUseCase,
    private val clearFavoritesUseCase: ClearFavoritesUseCase,
    private val clearBarUseCase: ClearBarUseCase,
    private val auth: FirebaseAuth
) : ViewModel() {

    val favoritesCount: LiveData<Int> = getFavoritesUseCase().map { it.size }
    val barCount: LiveData<Int> = getBarIngredientsUseCase().map { it.size }

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
                syncFavoritesUseCase()
                syncBarUseCase()
            } catch (_: Exception) { }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    fun clearFavorites() {
        viewModelScope.launch { clearFavoritesUseCase() }
    }

    fun clearBar() {
        viewModelScope.launch { clearBarUseCase() }
    }
}
