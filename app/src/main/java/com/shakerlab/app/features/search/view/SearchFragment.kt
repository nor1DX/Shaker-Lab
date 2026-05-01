package com.shakerlab.app.features.search.view

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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.shakerlab.app.NavGraphDirections
import com.shakerlab.app.databinding.FragmentSearchBinding
import com.shakerlab.app.features.catalog.view.CocktailPreviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val vm: SearchVM by viewModel()
    private val adapter = CocktailPreviewAdapter { preview ->
        val action = NavGraphDirections.actionGlobalDetail(preview.id)
        findNavController().navigate(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerResults.layoutManager = layoutManager
        binding.recyclerResults.adapter = adapter
        binding.recyclerResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (total > 0 && lastVisible >= total - 4) vm.loadMore()
            }
        })

        binding.btnSearch.setOnClickListener { submitSearch() }

        binding.editSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                submitSearch()
                true
            } else false
        }

        binding.btnClearRecent.setOnClickListener {
            vm.clearRecentSearches()
        }

        vm.results.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        vm.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        vm.isEmpty.observe(viewLifecycleOwner) { empty ->
            binding.textEmpty.isVisible = empty
        }

        vm.recentSearches.observe(viewLifecycleOwner) { recent ->
            binding.layoutRecent.isVisible = recent.isNotEmpty()
            buildRecentChips(recent)
        }
    }

    private fun submitSearch() {
        val query = binding.editSearch.text?.toString()?.trim() ?: return
        if (query.isEmpty()) return
        hideKeyboard()
        vm.search(query)
    }

    private fun buildRecentChips(searches: List<String>) {
        binding.chipGroupRecent.removeAllViews()
        val darkSurface = android.content.res.ColorStateList.valueOf(0xFF252525.toInt())
        val gray = 0xFF999999.toInt()

        searches.forEach { query ->
            val chip = Chip(requireContext()).apply {
                text = query
                isClickable = true
                chipBackgroundColor = darkSurface
                setTextColor(gray)
                chipStrokeColor = android.content.res.ColorStateList.valueOf(0xFF3A3A3A.toInt())
                chipStrokeWidth = resources.displayMetrics.density * 1f
                setOnClickListener {
                    binding.editSearch.setText(query)
                    vm.search(query)
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