package cherryjam.narfu.arkhdialect.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityTextAttachmentEditBinding

class TextAttachmentEditActivity : AppCompatActivity() {
    private val binding: ActivityTextAttachmentEditBinding by lazy {
        ActivityTextAttachmentEditBinding.inflate(layoutInflater)
    }

    var isNewAttachment: Boolean = false
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
        binding.attachment = attachment

        isNewAttachment = intent.getBooleanExtra("new", false)
    }

    override fun onStop() {
        super.onStop()
        Thread {
            val dao = database.textAttachmentDao()
            if (isNewAttachment) {
                if (attachment.isEmpty())
                    return@Thread
                dao.insert(attachment)
            }
            else {
                dao.update(attachment)
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}