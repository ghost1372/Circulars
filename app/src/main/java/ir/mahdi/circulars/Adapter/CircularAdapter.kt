package ir.mahdi.circulars.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import ir.mahdi.circulars.Helper.Prefs
import ir.mahdi.circulars.Model.CircularModel
import ir.mahdi.circulars.R
import kotlinx.android.synthetic.main.recyclerview_item.view.*
import java.util.*
import kotlin.collections.ArrayList

class CircularAdapter (private val itemsCells: ArrayList<CircularModel>, private val listener: CircularsAdapterListener) :
    RecyclerView.Adapter<CircularAdapter.ViewHolder>(), Filterable {

    lateinit var context: Context
    var itemsFilterList = ArrayList<CircularModel>()

    init {
        itemsFilterList = itemsCells
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        val vh = ViewHolder(v)
        context = parent.context
        return vh
    }

    override fun getItemCount(): Int {
        return itemsFilterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Add data to cells
        holder.itemView.txt_Title.text = itemsFilterList[position].title
        holder.itemView.txt_Status.text = itemsFilterList[position].status
        holder.itemView.txt_Date.text = itemsFilterList[position].date

        holder.itemView.icon_text.setText(itemsFilterList[position].title.substring( 0 , 1 ));
        holder.itemView.icon_profile.setImageResource(R.drawable.bg_circle)

        // If Theme is Not Dark We set Colors for Icon and If Theme is Dark We Dont Need Colors
        if (!Prefs(context).getIsDark()){
            holder.itemView.icon_profile.setColorFilter(itemsFilterList[position].color)
        }

        holder.itemView.setOnClickListener(View.OnClickListener {
            // send selected item in callback
            listener.onCircularSelected(itemsFilterList.get(position))
        })
    }

    // Search in List
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch: String = constraint.toString()

                if (charSearch.isEmpty()) {
                    itemsFilterList = itemsCells
                } else {
                    val resultList = ArrayList<CircularModel>()
                    for (row in itemsCells) {
                        if (row.title.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) || row.date.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)))
                        {
                            resultList.add(row)
                        }
                    }
                    itemsFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = itemsFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                itemsFilterList = results?.values as ArrayList<CircularModel>
                notifyDataSetChanged()
            }

        }
    }

    // RecyclerView Item Click Interface
    interface CircularsAdapterListener {
        fun onCircularSelected(item: CircularModel?)
    }
}