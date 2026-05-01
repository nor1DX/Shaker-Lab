package com.shakerlab.app.features.catalog.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.shakerlab.app.databinding.ItemCocktailPreviewBinding
import com.shakerlab.app.domain.model.CocktailPreview

class CocktailPreviewAdapter(
    private val onClick: (CocktailPreview) -> Unit
) : ListAdapter<CocktailPreview, CocktailPreviewAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCocktailPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CocktailPreview) {
            binding.textName.text = item.name
            binding.textCategory.text = item.category.ifEmpty { "—" }
            binding.imageCocktail.load(item.thumbnail) {
                crossfade(true)
            }
            binding.root.setOnClickListener { onClick(item) }
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

    class DiffCallback : DiffUtil.ItemCallback<CocktailPreview>() {
        override fun areItemsTheSame(oldItem: CocktailPreview, newItem: CocktailPreview) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CocktailPreview, newItem: CocktailPreview) =
            oldItem == newItem
    }
}