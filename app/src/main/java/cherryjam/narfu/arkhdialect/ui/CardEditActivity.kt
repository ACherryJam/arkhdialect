package cherryjam.narfu.arkhdialect.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.ActivityCardEditBinding

class CardEditActivity : AppCompatActivity() {
    private val binding: ActivityCardEditBinding by lazy {
        ActivityCardEditBinding.inflate(layoutInflater)
    }

    lateinit var card: Card

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


    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}