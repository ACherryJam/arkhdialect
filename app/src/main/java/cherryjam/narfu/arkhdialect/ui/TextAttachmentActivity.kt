package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cherryjam.narfu.arkhdialect.adapter.TextAttachmentAdapter
import cherryjam.narfu.arkhdialect.databinding.ActivityTextAttachmentBinding
import cherryjam.narfu.arkhdialect.service.attachment.FakerTextAttachmentService
import cherryjam.narfu.arkhdialect.service.attachment.TextAttachmentService

class TextAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityTextAttachmentBinding by lazy {
        ActivityTextAttachmentBinding.inflate(layoutInflater)
    }

    val service: TextAttachmentService = FakerTextAttachmentService()
    lateinit var adapter: TextAttachmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = TextAttachmentAdapter(this)
        adapter.data = service.getData()

        binding.attachmentList.adapter = adapter

        binding.addTextAttachment.setOnClickListener {
            val intent = Intent(this, TextAttachmentEditActivity::class.java)
            startActivity(intent)
        }
    }
}