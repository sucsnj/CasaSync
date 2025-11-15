package com.devminds.casasync

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class GenericAdapter<T>(
    private val items: MutableList<T>,
    private val layoutResId: Int,
    private val bind: (View, T, Int, GenericViewHolder) -> Unit,
    private val onItemClick: (T) -> Unit,
    private val onItemLongClick: ((T) -> Boolean)? = null
) : RecyclerView.Adapter<GenericAdapter<T>.GenericViewHolder>() {

    inner class GenericViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItem(item: T, position: Int) {
            bind(itemView, item, position, this)
            itemView.setOnClickListener { onItemClick(item) }
            onItemLongClick?.let { longClick ->
                itemView.setOnLongClickListener {
                    longClick(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return GenericViewHolder(view)
    }

    override fun onBindViewHolder(holder: GenericViewHolder, position: Int) {
        holder.bindItem(items[position], position)
    }

    override fun getItemCount() = items.size

}
