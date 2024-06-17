package cherryjam.narfu.arkhdialect.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityTextAttachmentEditBinding

class TextAttachmentEditActivity : AppCompatActivity() {
    private val binding: ActivityTextAttachmentEditBinding by lazy {
        ActivityTextAttachmentEditBinding.inflate(layoutInflater)
    }

    lateinit var attachment: TextAttachment
    private val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        attachment = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("attachment", TextAttachment::class.java)
        } else {
            intent.getParcelableExtra("attachment")
        } ?: throw IllegalArgumentException("No attachment passed to TextAttachmentEditActivity")

        binding.title.setText(attachment.title)
        binding.data.setText(attachment.content)
    }

    override fun onStop() {
        super.onStop()
        Thread {
            attachment.title = binding.title.text.toString()
            attachment.content = binding.data.text.toString()

            database.textAttachmentDao().update(attachment)
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}