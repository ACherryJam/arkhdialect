package cherryjam.narfu.arkhdialect.adapter

import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding
import cherryjam.narfu.arkhdialect.ui.CardEditActivity

class CardAdapter() : SelectableAdapter<CardAdapter.CardViewHolder>() {
    var data: List<Card> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    inner class CardViewHolder(val binding: ItemInterviewBinding)
        : ViewHolder(binding.root), SelectableItem {
        private val context = binding.root.context
        private lateinit var card: Card

        init {
            binding.listItem.setOnClickListener {
                val intent = Intent(context, CardEditActivity::class.java)
                intent.putExtra("card", card)
                context.startActivity(intent)
            }
        }

        fun onBind(card: Card) {
            this.card = card

            with (binding.listItem) {
                headline.text = card.word
                supportText.text = card.location
            }

            if (isItemSelected(bindingAdapterPosition)) onSelect() else onDeselect()
        }

        override fun onSelect() {
            val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val color = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.color.item_background_selected_night
                else -> R.color.item_background_selected_day
            }
            binding.listItem.setBackgroundResource(color)
        }

        override fun onDeselect() {
            val nightModeFlags: Int = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val color = when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> R.color.item_background_night
                else -> R.color.item_background_day
            }
            binding.listItem.setBackgroundResource(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInterviewBinding.inflate(inflater, parent, false)

        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = data[position]
        holder.onBind(card)
    }

    override fun getItemCount(): Int {
        return data.size
    }
}