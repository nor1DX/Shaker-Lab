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
import com.google.android.material.chip.Chip
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shakerlab.app.NavGraphDirections
import com.shakerlab.app.R
import com.shakerlab.app.databinding.FragmentCatalogBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val vm: CatalogVM by viewModel()

    private val adapter = CocktailPreviewAdapter { preview ->
        val action = NavGraphDirections.actionGlobalDetail(preview.id)
        findNavController().navigate(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabRandom.setOnClickListener { vm.getRandom() }

        vm.randomId.observe(viewLifecycleOwner) { id ->
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
                if (total > 0 && lastVisible >= total - 4) vm.loadMore()
            }
        })

        binding.chipGroupCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val chip = group.findViewById<Chip>(id)
            vm.loadByCategory(chip.text.toString())
        }

        vm.categories.observe(viewLifecycleOwner) { categories ->
            binding.chipGroupCategories.removeAllViews()
            categories.forEach { category ->
                val chip = buildFilterChip(category)
                binding.chipGroupCategories.addView(chip)
            }
            (binding.chipGroupCategories.getChildAt(0) as? Chip)?.isChecked = true
        }

        vm.cocktails.observe(viewLifecycleOwner) { cocktails ->
            adapter.submitList(cocktails)
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        vm.error.observe(viewLifecycleOwner) { error ->
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