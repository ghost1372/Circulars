package ir.mahdi.circulars.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import ir.mahdi.circulars.Model.IntroSliderModel
import ir.mahdi.circulars.R
import kotlinx.android.synthetic.main.slider_item_container.view.*

class IntroSliderAdapter(private val introSlides: List<IntroSliderModel>) :
    RecyclerView.Adapter<IntroSliderAdapter.IntroSliderViewholder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroSliderViewholder {
        return IntroSliderViewholder(LayoutInflater.from(parent.context).inflate(R.layout.slider_item_container,parent,false))
    }

    override fun getItemCount(): Int {
        return introSlides.size
    }

    override fun onBindViewHolder(holder: IntroSliderViewholder, position: Int) {
        holder.bind(introSlides[position])
    }

    inner class IntroSliderViewholder(view: View) : RecyclerView.ViewHolder(view){
        private val textTitle = view.findViewById<MaterialTextView>(R.id.textTitle)
        private val textDescription = view.findViewById<MaterialTextView>(R.id.textDescription)
        private val imageIcon = view.findViewById<AppCompatImageView>(R.id.imageSlideIcon)

        fun bind(introSlides: IntroSliderModel) {
            textTitle.text = introSlides.title
            textDescription.text = introSlides.description
            imageIcon.setImageResource(introSlides.icon)
        }
    }
}