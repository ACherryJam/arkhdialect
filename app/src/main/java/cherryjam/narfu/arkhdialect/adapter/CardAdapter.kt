package cherryjam.narfu.arkhdialect.adapter

import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding
import cherryjam.narfu.arkhdialect.ui.CardEditActivity
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper

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
                if (isSelecting)
                    selectItem(this)
                else
                    openEditor()
            }
            binding.listItem.setOnLongClickListener {
                if (!isSelecting)
                    startSelection()
                selectItem(this)
                true
            }
            binding.listItemOptions.setOnClickListener {
                val popup = PopupMenu(it.context, it)
                popup.inflate(R.menu.options_menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.open -> {
                            openEditor()
                            true
                        }
                        R.id.select -> {
                            if (!isSelecting)
                                startSelection()
                            selectItem(this)
                            true
                        }
                        R.id.delete -> {
                            AlertDialogHelper.showAlertDialog(
                                context,
                                title = context.getString(R.string.delete_card_title),
                                message = context.getString(R.string.delete_card_message),
                                positiveText = context.getString(R.string.delete),
                                negativeText = context.getString(R.string.cancel),
                                positiveCallback = {
                                    Thread {
                                        AppDatabase.getInstance(context).cardDao().delete(card)
                                    }.start()
                                },
                            )
                            true//
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        fun onBind(card: Card) {
            this.card = card

            with (binding.listItem) {
                with (card) {
                    headline.text = if (word.isEmpty())
                        context.getString(R.string.empty_card_word)
                    else
                        word

                    supportText.text = if (location.isEmpty())
                        context.getString(R.string.empty_card_district)
                    else
                        location
                }
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

        fun openEditor() {
            val intent = Intent(context, CardEditActivity::class.java)
            intent.putExtra("card", card)
            context.startActivity(intent)
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