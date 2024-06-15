package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.Interview
import cherryjam.narfu.arkhdialect.databinding.ActivityInterviewEditBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.reflect.KClass

class InterviewEditActivity : AppCompatActivity() {
    private val binding: ActivityInterviewEditBinding by lazy {
        ActivityInterviewEditBinding.inflate(layoutInflater)
    }

    lateinit var interview: Interview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        interview = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("interview", Interview::class.java)
        } else {
            intent.getParcelableExtra("interview")
        } ?: Interview(1111)

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            binding.photoAttachment.isEnabled = false
            Snackbar.make(
                binding.root, getString(R.string.cant_take_photo), Snackbar.LENGTH_SHORT
            ).show()
        }

        binding.textAttachment.setOnClickListener {
            startAttachmentActivity(TextAttachmentActivity::class)
        }

        binding.photoAttachment.setOnClickListener {
            startAttachmentActivity(PhotoAttachmentActivity::class)
        }

        binding.recordingAttachment.setOnClickListener {
            startAttachmentActivity(RecordingAttachmentActivity::class)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    fun <T : Any> startAttachmentActivity(activity: KClass<T>) {
        val intent = Intent(this, activity.java)
        intent.putExtra("interview", interview)
        startActivity(intent)
    }
}