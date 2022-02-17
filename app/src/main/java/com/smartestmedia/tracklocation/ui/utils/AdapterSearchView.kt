package com.smartestmedia.tracklocation.ui.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.smartestmedia.tracklocation.R
import com.smartestmedia.tracklocation.ui.model.Place
import com.smartestmedia.tracklocation.ui.utils.AppUtils.hideKeyboard
import kotlinx.android.synthetic.main.item_search_place.view.*

class SearchViewAdapter(
    var listdata: ArrayList<Place>,
    private val itemClickListener: (Place) -> Unit
) :
    RecyclerView.Adapter<SearchViewAdapter.DataViewHolder>(),
    Filterable {

    lateinit var listdataSearch: ArrayList<Place>


    init {
        listdataSearch = ArrayList()
        listdataSearch.addAll(listdata)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        return DataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_search_place, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return listdataSearch.size
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.nameTv.text = listdataSearch[position].name
        holder.openTv.text = listdataSearch[position].closeoropen
        holder.constraintCl.setOnClickListener {
            itemClickListener(listdataSearch[position])
            it?.hideKeyboard()
        }

    }

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var nameTv = itemView.name
        var openTv = itemView.open
        var constraintCl = itemView.item_search_cl

    }

    override fun getFilter(): Filter {


        return object : Filter() {
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val listFilter = ArrayList<Place>()
                if (p0 == null || p0.isEmpty()) {
                    listFilter.addAll(listdata)
                } else {
                    val filterPattern: String = p0.toString().toLowerCase().trim()
                    for (item in listdata) {
                        if (item.name.toLowerCase().contains(filterPattern)) {
                            listFilter.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = listFilter
                return results;
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {

                listdataSearch.clear()
                listdataSearch.addAll(p1?.values as Collection<Place>)

                notifyDataSetChanged()
            }
        }


    }

}