package fr.jorisfavier.youshallnotpass.features.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.models.Item
import fr.jorisfavier.youshallnotpass.utils.inflate

class SearchResultAdapter(private var results: List<Item>) : RecyclerView.Adapter<SearchResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = parent.inflate(R.layout.viewholder_search_result,false)
        return SearchResultViewHolder(view)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    fun updateResults(newList: List<Item>) {
        results = newList
        notifyDataSetChanged()
    }

}