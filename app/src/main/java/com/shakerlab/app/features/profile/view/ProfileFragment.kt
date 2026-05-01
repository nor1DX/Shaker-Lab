package com.shakerlab.app.features.profile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shakerlab.app.databinding.FragmentProfileBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val vm: ProfileVM by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.favoritesCount.observe(viewLifecycleOwner) { count ->
            binding.textFavoritesCount.text = count.toString()
        }

        vm.barCount.observe(viewLifecycleOwner) { count ->
            binding.textBarCount.text = count.toString()
        }

        binding.btnClearFavorites.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear favorites")
                .setMessage("Remove all favorite cocktails?")
                .setPositiveButton("Clear") { _, _ -> vm.clearFavorites() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.btnClearBar.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear bar")
                .setMessage("Remove all bar ingredients?")
                .setPositiveButton("Clear") { _, _ -> vm.clearBar() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}