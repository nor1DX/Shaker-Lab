package com.shakerlab.app.features.search.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.shakerlab.app.NavGraphDirections
import com.shakerlab.app.databinding.FragmentSearchBinding
import com.shakerlab.app.features.catalog.view.CocktailPreviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private val adapter = CocktailPreviewAdapter(
        onClick = { preview -> findNavController().navigate(NavGraphDirections.actionGlobalDetail(preview.id)) },
        onFavorite = { preview -> viewModel.toggleFavorite(preview) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerResults.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerResults.adapter = adapter

        binding.btnSearch.setOnClickListener { submitSearch() }

        binding.editSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                submitSearch(); true
            } else false
        }

        binding.btnClearRecent.setOnClickListener { viewModel.clearRecentSearches() }

        setupFilterChips()

        viewModel.results.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.chipGroupFilter.isVisible = list.isNotEmpty()
        }

        viewModel.favoriteIds.observe(viewLifecycleOwner) { ids -> adapter.favoriteIds = ids }

        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBar.isVisible = it }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        viewModel.isEmpty.observe(viewLifecycleOwner) { binding.textEmpty.isVisible = it }

        viewModel.recentSearches.observe(viewLifecycleOwner) { recent ->
            binding.layoutRecent.isVisible = recent.isNotEmpty()
            buildRecentChips(recent)
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.isVisible = false
        listOf("All", "Alcoholic", "Non-alcoholic").forEach { label ->
            val chip = buildFilterChip(label)
            if (label == "All") chip.isChecked = true
            chip.setOnClickListener { viewModel.setAlcoholicFilter(label) }
            binding.chipGroupFilter.addView(chip)
        }
    }

    private fun buildFilterChip(text: String): Chip {
        val gold = 0xFFF5C842.toInt()
        val darkSurface = 0xFF252525.toInt()
        val black = 0xFF000000.toInt()
        val gray = 0xFF999999.toInt()
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
                intArrayOf(gold, 0xFF3A3A3A.toInt())
            )
            chipStrokeWidth = resources.displayMetrics.density * 1.5f
        }
    }

    private fun submitSearch() {
        val query = binding.editSearch.text?.toString()?.trim() ?: return
        if (query.isEmpty()) return
        hideKeyboard()
        viewModel.search(query)
    }

    private fun buildRecentChips(searches: List<String>) {
        binding.chipGroupRecent.removeAllViews()
        val darkSurface = ColorStateList.valueOf(0xFF252525.toInt())
        searches.forEach { query ->
            val chip = Chip(requireContext()).apply {
                text = query
                isClickable = true
                chipBackgroundColor = darkSurface
                setTextColor(0xFF999999.toInt())
                chipStrokeColor = ColorStateList.valueOf(0xFF3A3A3A.toInt())
                chipStrokeWidth = resources.displayMetrics.density * 1f
                setOnClickListener {
                    binding.editSearch.setText(query)
                    viewModel.search(query)
                    hideKeyboard()
                }
            }
            binding.chipGroupRecent.addView(chip)
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(binding.editSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
