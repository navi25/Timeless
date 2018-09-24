package io.navendra.timeless.views.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.navendra.timeless.R
import io.navendra.timeless.models.TimelessFilter
import kotlinx.android.synthetic.main.filter_item.view.*

class FiltersAdapter(private val filters:List<TimelessFilter>) : RecyclerView.Adapter<FiltersAdapter.FilterViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, pos: Int): FilterViewHolder {
       val itemView =  LayoutInflater.from(parent.context).inflate(R.layout.filter_item,parent,false)
        return FilterViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    override fun onBindViewHolder(holder: FilterViewHolder, pos: Int) {
        val filter = filters[pos]
        holder.image.setImageResource(filter.image)
        holder.title.text = filter.name
    }

    inner class FilterViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView){
        val title = itemView.title
        val image = itemView.image
    }
}