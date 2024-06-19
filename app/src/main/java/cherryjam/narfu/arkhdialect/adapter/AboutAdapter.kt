package cherryjam.narfu.arkhdialect.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.databinding.ItemAboutBinding

class AboutAdapter(val context: Context) : RecyclerView.Adapter<AboutAdapter.AboutViewHolder>() {
    var data: List<Map<String, String>> = listOf(
        mapOf("title" to "Матвиенко Даниил Владимирович", "info" to "3 курс ПМИ 151112"),
        mapOf("title" to "Стариков Александр Алексеевич", "info" to "3 курс ПМИ 151112")
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