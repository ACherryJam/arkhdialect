package cherryjam.narfu.arkhdialect.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityTextAttachmentEditBinding

class TextAttachmentEditActivity : AppCompatActivity() {
    private val binding: ActivityTextAttachmentEditBinding by lazy {
        ActivityTextAttachmentEditBinding.inflate(layoutInflater)
    }

    lateinit var attachment: TextAttachment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val parcel: TextAttachment? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("attachment", TextAttachment::class.java)
        } else {
            intent.getParcelableExtra("attachment")
        }

        attachment = parcel ?: TextAttachment(11111)

        binding.title.setText(attachment.title)
        binding.data.setText(attachment.data)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}