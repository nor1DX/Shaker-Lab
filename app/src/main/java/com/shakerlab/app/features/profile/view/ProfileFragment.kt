package com.shakerlab.app.features.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shakerlab.app.R
import com.shakerlab.app.databinding.FragmentProfileBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModel()

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleSignInResult(result.data)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.favoritesCount.observe(viewLifecycleOwner) { count ->
            binding.textFavoritesCount.text = count.toString()
        }

        viewModel.barCount.observe(viewLifecycleOwner) { count ->
            binding.textBarCount.text = count.toString()
        }

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.textUserName.text = user.displayName ?: "User"
                binding.textUserEmail.text = user.email ?: ""
                binding.btnSignIn.isVisible = false
                binding.btnSignOut.isVisible = true
            } else {
                binding.textUserName.text = "Guest"
                binding.textUserEmail.text = "Not signed in"
                binding.btnSignIn.isVisible = true
                binding.btnSignOut.isVisible = false
            }
        }

        binding.btnSignIn.setOnClickListener {
            signInLauncher.launch(buildSignInIntent())
        }

        binding.btnSignOut.setOnClickListener {
            GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            viewModel.signOut()
        }

        binding.btnClearFavorites.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear favorites")
                .setMessage("Remove all favorite cocktails?")
                .setPositiveButton("Clear") { _, _ -> viewModel.clearFavorites() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnClearBar.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear bar")
                .setMessage("Remove all bar ingredients?")
                .setPositiveButton("Clear") { _, _ -> viewModel.clearBar() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun buildSignInIntent() = GoogleSignIn.getClient(
        requireContext(),
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    ).signInIntent

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
