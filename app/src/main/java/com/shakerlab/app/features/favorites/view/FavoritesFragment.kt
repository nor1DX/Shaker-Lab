package com.shakerlab.app.features.favorites.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.shakerlab.app.NavGraphDirections
import com.shakerlab.app.R
import com.shakerlab.app.databinding.FragmentFavoritesBinding
import com.shakerlab.app.features.catalog.view.CocktailPreviewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val vm: FavoritesVM by viewModel()
    private val adapter = CocktailPreviewAdapter(
        onClick = { preview -> findNavController().navigate(NavGraphDirections.actionGlobalDetail(preview.id)) },
        onFavorite = { preview -> vm.toggleFavorite(preview) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerFavorites.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerFavorites.adapter = adapter

        binding.btnGoToCatalog.setOnClickListener {
            findNavController().navigate(R.id.catalogFragment)
        }

        vm.favorites.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.textEmpty.isVisible = list.isEmpty()
            binding.recyclerFavorites.isVisible = list.isNotEmpty()
        }

        vm.favoriteIds.observe(viewLifecycleOwner) { ids -> adapter.favoriteIds = ids }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
