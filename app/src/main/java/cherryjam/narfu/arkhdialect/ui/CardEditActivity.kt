package cherryjam.narfu.arkhdialect.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.databinding.ActivityCardEditBinding

class CardEditActivity : AppCompatActivity() {
    private val binding: ActivityCardEditBinding by lazy {
        ActivityCardEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}