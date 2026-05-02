package com.shakerlab.app.features.mybar.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shakerlab.app.NavGraphDirections
import com.shakerlab.app.R
import com.shakerlab.app.databinding.FragmentMyBarBinding
import com.shakerlab.app.features.catalog.view.CocktailPreviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyBarFragment : Fragment() {

    private var _binding: FragmentMyBarBinding? = null
    private val binding get() = _binding!!

    private val vm: MyBarVM by viewModel()
    private val adapter = CocktailPreviewAdapter(
        onClick = { preview -> findNavController().navigate(NavGraphDirections.actionGlobalDetail(preview.id)) },
        onFavorite = { preview -> vm.toggleFavorite(preview) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerMyBar.layoutManager = layoutManager
        binding.recyclerMyBar.adapter = adapter
        binding.recyclerMyBar.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (total > 0 && lastVisible >= total - 4) vm.loadMore()
            }
        })

        binding.btnAddIngredient.setOnClickListener {
            showAddIngredientDialog()
        }

        binding.chipGroupFilter.isVisible = false

        vm.barIngredients.observe(viewLifecycleOwner) { ingredients ->
            renderChips(ingredients)
        }

        vm.cocktails.observe(viewLifecycleOwner) { cocktails ->
            adapter.submitList(cocktails)
            updateEmptyState()
        }

        vm.favoriteIds.observe(viewLifecycleOwner) { ids -> adapter.favoriteIds = ids }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            updateEmptyState()
        }
    }

    private fun updateEmptyState() {
        val loading = vm.isLoading.value ?: false
        val ingredients = vm.barIngredients.value ?: emptyList()
        val cocktails = vm.cocktails.value ?: emptyList()

        val showEmpty = !loading && (ingredients.isEmpty() || cocktails.isEmpty())
        binding.textEmpty.isVisible = showEmpty

        binding.textEmptyTitle.text = if (ingredients.isEmpty()) {
            "Add ingredients\nto find cocktails"
        } else {
            "No cocktails found\nwith these ingredients"
        }
    }

    private fun renderChips(ingredients: List<String>) {
        binding.chipGroupIngredients.removeAllViews()
        val gold = 0xFFF5C842.toInt()
        val darkSurface = 0xFF2A2218.toInt()

        ingredients.forEach { name ->
            val chip = Chip(requireContext()).apply {
                text = name
                isCloseIconVisible = true
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(darkSurface)
                setTextColor(gold)
                closeIconTint = android.content.res.ColorStateList.valueOf(gold)
                chipStrokeColor = android.content.res.ColorStateList.valueOf(gold)
                chipStrokeWidth = resources.displayMetrics.density * 1f
                setOnCloseIconClickListener { vm.removeIngredient(name) }
            }
            binding.chipGroupIngredients.addView(chip)
        }
        updateEmptyState()
    }

    private fun showAddIngredientDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_ingredient, null)
        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.auto_complete_ingredient)

        val allIngredients = vm.allIngredients.value ?: emptyList()
        autoComplete.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allIngredients)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add ingredient")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = autoComplete.text.toString().trim()
                if (name.isNotEmpty()) vm.addIngredient(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
