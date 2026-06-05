package com.shakerlab.app.features.profile.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.shakerlab.app.R
import com.shakerlab.app.ui.CardSurface
import com.shakerlab.app.ui.Gold
import com.shakerlab.app.ui.TextGray
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen() {
    val viewModel: ProfileViewModel = koinViewModel()
    val context = LocalContext.current

    val currentUser by viewModel.currentUser.observeAsState(null)
    val favoritesCount by viewModel.favoritesCount.observeAsState(0)
    val barCount by viewModel.barCount.observeAsState(0)

    var showClearFavDialog by remember { mutableStateOf(false) }
    var showClearBarDialog by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.handleSignInResult(result.data) }

    if (showClearFavDialog) {
        AlertDialog(
            onDismissRequest = { showClearFavDialog = false },
            title = { Text("Clear favorites") },
            text = { Text("Remove all favorite cocktails?") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearFavorites(); showClearFavDialog = false }) {
                    Text("Clear", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearFavDialog = false }) { Text("Cancel") }
            },
            containerColor = CardSurface
        )
    }

    if (showClearBarDialog) {
        AlertDialog(
            onDismissRequest = { showClearBarDialog = false },
            title = { Text("Clear bar") },
            text = { Text("Remove all bar ingredients?") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearBar(); showClearBarDialog = false }) {
                    Text("Clear", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearBarDialog = false }) { Text("Cancel") }
            },
            containerColor = CardSurface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Profile",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp)
        )

        // Avatar
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(CardSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = currentUser?.displayName ?: "Guest",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = currentUser?.email ?: "Not signed in",
            color = TextGray,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Sign In / Sign Out
        if (currentUser == null) {
            androidx.compose.material3.Button(
                onClick = {
                    val intent = GoogleSignIn.getClient(
                        context,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                    ).signInIntent
                    signInLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.Black
                )
            ) {
                Text("Sign in with Google", fontWeight = FontWeight.Bold)
            }
        } else {
            OutlinedButton(
                onClick = {
                    GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
                    viewModel.signOut()
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E2E))
            ) {
                Text("Sign out")
            }
        }

        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                number = favoritesCount,
                label = "Favorites",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                number = barCount,
                label = "Bar ingredients",
                modifier = Modifier.weight(1f)
            )
        }

        // Action buttons
        OutlinedButton(
            onClick = { showClearFavDialog = true },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E2E))
        ) {
            Text("Clear favorites")
        }

        OutlinedButton(
            onClick = { showClearBarDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextGray),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2E2E))
        ) {
            Text("Clear bar ingredients")
        }
    }
}

@Composable
private fun StatCard(number: Int, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = number.toString(),
                color = Gold,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = TextGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
