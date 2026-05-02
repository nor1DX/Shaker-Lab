package com.shakerlab.app.features.mybar.view

import android.content.res.ColorStateList
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

    private val viewModel: MyBarViewModel by viewModel()
    private val adapter = CocktailPreviewAdapter(
        onClick = { preview -> findNavController().navigate(NavGraphDirections.actionGlobalDetail(preview.id)) },
        onFavorite = { preview -> viewModel.toggleFavorite(preview) }
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
                if (total > 0 && lastVisible >= total - 4) viewModel.loadMore()
            }
        })

        binding.btnAddIngredient.setOnClickListener {
            showAddIngredientDialog()
        }

        setupFilterChips()

        viewModel.barIngredients.observe(viewLifecycleOwner) { ingredients ->
            renderChips(ingredients)
        }

        viewModel.cocktails.observe(viewLifecycleOwner) { cocktails ->
            adapter.submitList(cocktails)
            updateEmptyState()
        }

        viewModel.favoriteIds.observe(viewLifecycleOwner) { ids -> adapter.favoriteIds = ids }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            updateEmptyState()
        }
    }

    private fun setupFilterChips() {
        val filters = listOf("All", "Alcoholic", "Non-Alcoholic")
        filters.forEach { label ->
            val chip = buildFilterChip(label)
            if (label == "All") chip.isChecked = true
            binding.chipGroupFilter.addView(chip)
        }
        binding.chipGroupFilter.isVisible = true
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(id)
            viewModel.setFilter(chip.text.toString())
        }
    }

    private fun buildFilterChip(text: String): Chip {
        val gold = 0xFFF5C842.toInt()
        val darkSurface = 0xFF252525.toInt()
        val black = 0xFF000000.toInt()
        val gray = 0xFF999999.toInt()
        val outline = 0xFF3A3A3A.toInt()
        return Chip(requireContext()).apply {
            this.text = text
            isCheckable = true
            chipBackgroundColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(gold, darkSurface)
            )
            setTextColor(ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(black, gray)
            ))
            chipStrokeColor = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(gold, outline)
            )
            chipStrokeWidth = resources.displayMetrics.density * 1.5f
        }
    }

    private fun updateEmptyState() {
        val loading = viewModel.isLoading.value ?: false
        val ingredients = viewModel.barIngredients.value ?: emptyList()
        val cocktails = viewModel.cocktails.value ?: emptyList()

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
                setOnCloseIconClickListener { viewModel.removeIngredient(name) }
            }
            binding.chipGroupIngredients.addView(chip)
        }
        updateEmptyState()
    }

    private fun showAddIngredientDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_ingredient, null)
        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.auto_complete_ingredient)

        val allIngredients = viewModel.allIngredients.value ?: emptyList()
        autoComplete.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, allIngredients)
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add ingredient")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = autoComplete.text.toString().trim()
                if (name.isNotEmpty()) viewModel.addIngredient(name)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
