package ir.mahdi.circulars.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Model.OfflineModel
import ir.mahdi.circulars.R
import kotlinx.android.synthetic.main.recyclerview_item_offline.view.*
import java.util.*
import kotlin.collections.ArrayList

class OfflineAdapter(private var listener: CircularsAdapterListener) :
    RecyclerView.Adapter<OfflineAdapter.ViewHolder>(), Filterable {

    var tracker: SelectionTracker<Long>? = null // For Creating Multi Select

    var temp = ArrayList<OfflineModel>() // For Enabling Search

    var itemsList = ArrayList<OfflineModel>()

    init {
        setHasStableIds(true) // We Need This for Multi Select
        temp = itemsList
    }
    lateinit var context: Context
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(isActivated: Boolean = false) {
            itemView.isActivated = isActivated // For multi Select
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long? = itemId
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item_offline, parent, false)
        val vh = ViewHolder(v)
        context = parent.context
        return vh
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }
    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Add data to cells
        holder.itemView.txt_offline.text = itemsList[position].name

        if (Prefs(context).getIsDark()) {
            holder.itemView.ly_MultiSelect.background =
                ContextCompat.getDrawable(context, R.drawable.rc_long_selected_dark)
        }else{
            holder.itemView.ly_MultiSelect.background =
                ContextCompat.getDrawable(context, R.drawable.rc_long_selected)        }

        holder.itemView.setOnClickListener(View.OnClickListener {
            // send selected item in callback
            listener.onCircularSelected(itemsList.get(position))
        })

        tracker?.let {
            holder.bind(it.isSelected(position.toLong()))
        }
    }

    // Search in List
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch: String = constraint.toString()
                if (charSearch.isEmpty()) {
                    itemsList = temp
                } else {
                    val resultList = ArrayList<OfflineModel>()
                    for (row in temp) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)))
                        {
                            resultList.add(row)
                        }
                    }
                    itemsList = resultList
                }

                val filterResults = FilterResults()
                filterResults.values = itemsList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                itemsList = results?.values as ArrayList<OfflineModel>
                notifyDataSetChanged()
            }

        }
    }

    // RecyclerView Item Click Interface
    interface CircularsAdapterListener {
        fun onCircularSelected(item: OfflineModel?)
    }

}