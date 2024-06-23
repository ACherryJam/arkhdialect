package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.databinding.ItemAboutBinding

class AboutAdapter(val context: Context) : RecyclerView.Adapter<AboutAdapter.AboutViewHolder>() {
    var data: List<Map<String, String>> = listOf(
        mapOf("title" to context.getString(R.string.first_full_name), "info" to context.getString(R.string.info)),
        mapOf("title" to context.getString(R.string.seconds_full_name), "info" to context.getString(R.string.info))
    )

    class AboutViewHolder(val binding: ItemAboutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAboutBinding.inflate(inflater, parent, false)

        return AboutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AboutViewHolder, position: Int) {
        val about = data[position]

        with(holder.binding) {
            with(listItem) {
                headline.text = about.getValue("title")
                supportText.text = about.getValue("info")
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }


}