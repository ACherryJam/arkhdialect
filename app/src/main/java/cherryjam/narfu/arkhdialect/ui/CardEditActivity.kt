package cherryjam.narfu.arkhdialect.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cherryjam.narfu.arkhdialect.adapter.SpecialSymbolAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.databinding.ActivityCardEditBinding
import cherryjam.narfu.arkhdialect.utils.CursorListenedEditText
import com.simplemobiletools.commons.compose.extensions.getActivity
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

class CardEditActivity : AppCompatActivity() {
    private val binding: ActivityCardEditBinding by lazy {
        ActivityCardEditBinding.inflate(layoutInflater)
    }

    lateinit var card: Card
    private val database by lazy { AppDatabase.getInstance(this) }

    private lateinit var symbolAdapter: SpecialSymbolAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        card = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("card", Card::class.java)
        } else {
            intent.getParcelableExtra("card")
        } ?: throw IllegalArgumentException("No Card entity passed to CardEditActivity")

        with(binding) {
            word.setText(card.word)
            location.setText(card.location)
            characteristics.setText(card.characteristics)
            meaning.setText(card.meaning)
            example.setText(card.example)
        }

        binding.undo.setOnClickListener {
            finish()
        }

        binding.save.setOnClickListener {
            Thread {
                card.word = binding.word.text.toString()
                card.location = binding.location.text.toString()
                card.characteristics = binding.characteristics.text.toString()
                card.meaning = binding.meaning.text.toString()
                card.example = binding.example.text.toString()

                database.cardDao().update(card)
                finish()
            }.start()
        }

        KeyboardVisibilityEvent.setEventListener(
            this.getActivity(),
            this
        ) { isOpen ->
            binding.buttons.visibility = if (isOpen) View.GONE else View.VISIBLE
            binding.keys.visibility = if (!isOpen) View.GONE else View.VISIBLE
        }

        symbolAdapter = SpecialSymbolAdapter()

        // TODO: Separate functionality into a separate class and refactor this lmao
        binding.example.addCursorListener(
            object : CursorListenedEditText.CursorListener {
                override fun onCursorChange(cursorPosition: Int) {
                    if (binding.example.text == null) {
                        symbolAdapter.data = listOf()
                        return
                    }

                    val text = binding.example.text.toString()

                    var previousSymbol: String = ""
                    var symbolSet: List<String> = emptyList()

                    for (symbols in SPECIAL_SYMBOLS) {
                        for (symbol in symbols) {
                            val len = symbol.length
                            if (cursorPosition-len < 0)
                                continue

                            if (text.substring(cursorPosition-len, cursorPosition) == symbol) {
                                previousSymbol = symbol
                                symbolSet = symbols
                                break
                            }
                        }

                        if (symbolSet.isNotEmpty())
                            break
                    }

                    symbolAdapter.updateSymbolData(
                        previousSymbol, symbolSet
                    ) { previous, symbol ->
                        val lengthDiff = previous.length - symbol.length
                        var text = (binding.example.text ?: return@updateSymbolData).toString()
                        text = text.substring(0, cursorPosition-previous.length) + symbol + text.substring(cursorPosition)
                        binding.example.setText(text)
                        binding.example.setSelection(cursorPosition-lengthDiff)
                    }
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()
        binding.keys.adapter = symbolAdapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        val SPECIAL_SYMBOLS: List<List<String>> = listOf(
            listOf("у", "у́", "ў", "ӱ", "ӱ́", "ӯ"),
            listOf("У", "У́", "Ў", "Ӱ", "Ӱ́", "Ӯ"),
            listOf("а", "а́", "ӑ", "ӓ", "а̄"),
            listOf("А", "А́", "Ӑ", "Ӓ", "А̄"),
            listOf("е", "е́", "ӗ", "ё", "ё́", "е̄"),
            listOf("Е", "Е́", "Ӗ", "Ё", "Ё́", "Е̄"),
            listOf("и", "и́", "й", "ҋ", "ӥ", "ӣ"),
            listOf("И", "И́", "Й", "Ҋ", "Ӥ", "Ӣ"),
            listOf("ы", "ы́", "ы̆", "ӹ", "ы̄"),
            listOf("Ы", "Ы́", "Ы̆", "Ӹ", "Ы̄"),
            listOf("э", "э́", "ӭ", "э̄"),
            listOf("Э", "Э́", "Ӭ", "Э̄"),
            listOf("ю", "ю́", "ю̆", "ю̈", "ю̄"),
            listOf("Ю", "Ю́", "Ю̆", "Ю̈", "Ю̄"),
            listOf("я", "я́", "я̆", "я̈", "я̄"),
            listOf("Я", "Я́", "Я̆", "Я̈", "Я̄"),
        )
    }
}