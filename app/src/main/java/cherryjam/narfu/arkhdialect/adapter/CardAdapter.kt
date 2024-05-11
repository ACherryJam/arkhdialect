package cherryjam.narfu.arkhdialect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.data.Card
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding

class CardAdapter() : Adapter<CardAdapter.CardViewHolder>() {
    var data: List<Card> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    constructor(data: List<Card>) : this() {
        this.data = data
    }

    class CardViewHolder(val binding: ItemInterviewBinding) : ViewHolder(binding.root)
    {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInterviewBinding.inflate(inflater, parent, false)

        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = data[position]

        with (holder.binding.listItem) {
            headline.text = card.word
            supportText.text = card.location
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}