package cherryjam.narfu.arkhdialect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cherryjam.narfu.arkhdialect.databinding.ItemSpecialSymbolBinding

class SpecialSymbolAdapter
    : RecyclerView.Adapter<SpecialSymbolAdapter.SpecialSymbolViewHolder>() {

    var data: List<String> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }
    var currentSymbol: String = ""
    private var symbolCallback: SymbolCallback = {_, _ ->}

    inner class SpecialSymbolViewHolder(val binding: ItemSpecialSymbolBinding)
        : RecyclerView.ViewHolder(binding.root) {
        private lateinit var symbol: String

        init {
            binding.key.setOnClickListener {
                symbolCallback(currentSymbol, symbol)
            }
        }

        fun onBind(symbol: String) {
            this.symbol = symbol
            binding.key.text = symbol
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialSymbolViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemSpecialSymbolBinding.inflate(layoutInflater, parent, false)

        return SpecialSymbolViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpecialSymbolViewHolder, position: Int) {
        holder.onBind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun updateSymbolData(currentSymbol: String, symbols: List<String>, callback: SymbolCallback) {
        data = symbols
        this.currentSymbol = currentSymbol
        symbolCallback = callback
    }
}

typealias SymbolCallback = (String, String) -> Unit