package com.shakerlab.app.features.catalog.view

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.shakerlab.app.NavGraphDirections
import com.shakerlab.app.R
import com.shakerlab.app.databinding.FragmentCatalogBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CatalogViewModel by viewModel()

    private val adapter = CocktailPreviewAdapter(
        onClick = { preview -> findNavController().navigate(NavGraphDirections.actionGlobalDetail(preview.id)) },
        onFavorite = { preview -> viewModel.toggleFavorite(preview) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabRandom.setOnClickListener { viewModel.getRandom() }

        viewModel.randomId.observe(viewLifecycleOwner) { id ->
            if (id != null) findNavController().navigate(NavGraphDirections.actionGlobalDetail(id))
        }

        binding.btnSettings.setOnClickListener {
            requireActivity()
                .findViewById<BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.profileFragment
        }

        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerCocktails.layoutManager = layoutManager
        binding.recyclerCocktails.adapter = adapter
        binding.recyclerCocktails.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val lastVisible = layoutManager.findLastVisibleItemPosition()
                val total = layoutManager.itemCount
                if (total > 0 && lastVisible >= total - 4) viewModel.loadMore()
            }
        })

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(id)
            viewModel.loadByCategory(chip.text.toString())
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (binding.chipGroupCategories.childCount > 0) return@observe
            categories.forEachIndexed { index, category ->
                val chip = buildFilterChip(category)
                if (index == 0) chip.isChecked = true
                binding.chipGroupCategories.addView(chip)
            }
        }

        viewModel.cocktails.observe(viewLifecycleOwner) { adapter.submitList(it) }

        viewModel.favoriteIds.observe(viewLifecycleOwner) { ids -> adapter.favoriteIds = ids }

        viewModel.isLoading.observe(viewLifecycleOwner) { binding.progressBar.isVisible = it }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
