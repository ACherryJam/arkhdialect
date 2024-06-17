package cherryjam.narfu.arkhdialect.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.databinding.ActivityCardEditBinding

class CardEditActivity : AppCompatActivity() {
    private val binding: ActivityCardEditBinding by lazy {
        ActivityCardEditBinding.inflate(layoutInflater)
    }

    lateinit var card: Card
    private val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}