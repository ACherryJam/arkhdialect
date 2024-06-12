package cherryjam.narfu.arkhdialect.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.Interview
import cherryjam.narfu.arkhdialect.databinding.ItemInterviewBinding
import cherryjam.narfu.arkhdialect.ui.InterviewEditActivity

class InterviewAdapter : SelectableAdapter<InterviewAdapter.InterviewViewHolder>() {
    var data: MutableList<Interview> = arrayListOf()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    class InterviewViewHolder(val binding: ItemInterviewBinding) : ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemInterviewBinding.inflate(inflater, parent, false)

        return InterviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterviewViewHolder, position: Int) {
        val interview = data[position]

        with (holder.binding) {
            with (listItem) {
                tag = interview

                headline.text = interview.name
                supportText.text = interview.location

                setOnClickListener {
                    if (isSelecting)
                        selectItem(position)
                    else {
                        val intent = Intent(context, InterviewEditActivity::class.java)
                        intent.putExtra("interview", interview)
                        context.startActivity(intent)
                    }
                }
                setOnLongClickListener {
                    if (!isSelecting)
                        startSelection()
                    selectItem(position)

                    false
                }

                val color = if (isItemSelected(position))
                    R.color.selected_item
                else
                    R.color.white
                setBackgroundResource(color)
            }

            with (listItemOptions) {
                setOnClickListener {
                    val popup = PopupMenu(it.context, it)
                    popup.inflate(R.menu.options_menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.select -> {
                                if (!isSelecting)
                                    startSelection()
                                selectItem(position)
                                true
                            }
                            R.id.delete -> {
                                // ADD DELETE CODE
                                // TO-DO: Figure out where to handle fucking service interactions
                                // because if it's gonna be in both adapter and fragment that'll
                                // be a huge mess
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}