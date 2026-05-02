package com.shakerlab.app.features.detail.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.shakerlab.app.R
import com.shakerlab.app.databinding.FragmentDetailBinding
import com.shakerlab.app.databinding.ItemIngredientBinding
import com.shakerlab.app.domain.model.Cocktail
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val vm: DetailVM by viewModel()

    private val cocktailId: String by lazy {
        DetailFragmentArgs.fromBundle(requireArguments()).cocktailId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnFavorite.setOnClickListener { vm.toggleFavorite() }
        binding.fabNextRandom.setOnClickListener { vm.getNextRandom() }

        vm.loadCocktail(cocktailId)

        vm.cocktail.observe(viewLifecycleOwner) { cocktail ->
            cocktail?.let { render(it) }
        }

        vm.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
            binding.scrollContent.isVisible = !loading
        }

        vm.error.observe(viewLifecycleOwner) { error ->
            if (error != null) Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        vm.isFavorite.observe(viewLifecycleOwner) { isFav ->
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorites else R.drawable.ic_favorites_outline
            )
        }
    }

    private fun render(cocktail: Cocktail) {
        binding.imageCocktail.load(cocktail.thumbnail) { crossfade(true) }
        binding.textName.text = cocktail.name
        binding.textCategory.text = cocktail.category
        binding.textAlcoholic.text = if (cocktail.isAlcoholic) "Alcoholic" else "Non-alcoholic"
        binding.textInstructions.text = cocktail.instructions
        binding.fabNextRandom.isVisible = true

        binding.containerIngredients.removeAllViews()
        cocktail.ingredients.forEach { ingredient ->
            val row = ItemIngredientBinding.inflate(layoutInflater, binding.containerIngredients, true)
            row.textIngredientName.text = ingredient.name
            row.textIngredientMeasure.text = ingredient.measure
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
