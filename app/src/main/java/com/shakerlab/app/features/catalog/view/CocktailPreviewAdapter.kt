package com.shakerlab.app.features.catalog.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shakerlab.app.R
import com.shakerlab.app.databinding.ItemCocktailPreviewBinding
import com.shakerlab.app.domain.model.CocktailPreview

class CocktailPreviewAdapter(
    private val onClick: (CocktailPreview) -> Unit,
    private val onFavorite: ((CocktailPreview) -> Unit)? = null
) : ListAdapter<CocktailPreview, CocktailPreviewAdapter.ViewHolder>(DiffCallback()) {

    var favoriteIds: Set<String> = emptySet()
        set(value) {
            field = value
            notifyItemRangeChanged(0, itemCount, PAYLOAD_FAV)
        }

    inner class ViewHolder(private val binding: ItemCocktailPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CocktailPreview) {
            binding.textName.text = item.name
            binding.textCategory.text = item.category.ifEmpty { "Cocktail" }
            binding.imageCocktail.load(item.thumbnail) { crossfade(true) }
            binding.root.setOnClickListener { onClick(item) }
            bindFavorite(item)
        }

        fun bindFavorite(item: CocktailPreview) {
            val isFav = item.id in favoriteIds
            binding.btnFavorite.setImageResource(
                if (isFav) R.drawable.ic_favorites else R.drawable.ic_favorites_outline
            )
            if (onFavorite != null) {
                binding.btnFavorite.setOnClickListener { onFavorite.invoke(item) }
                binding.btnFavorite.isVisible = true
            } else {
                binding.btnFavorite.isVisible = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCocktailPreviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.contains(PAYLOAD_FAV)) holder.bindFavorite(getItem(position))
        else super.onBindViewHolder(holder, position, payloads)
    }

    class DiffCallback : DiffUtil.ItemCallback<CocktailPreview>() {
        override fun areItemsTheSame(oldItem: CocktailPreview, newItem: CocktailPreview) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CocktailPreview, newItem: CocktailPreview) =
            oldItem == newItem
    }

    companion object {
        private const val PAYLOAD_FAV = "fav"
    }
}
