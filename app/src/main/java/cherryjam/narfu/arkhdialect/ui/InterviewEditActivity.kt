package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.ActivityInterviewEditBinding
import com.google.android.material.snackbar.Snackbar
import kotlin.reflect.KClass

class InterviewEditActivity : AppCompatActivity() {
    private val binding: ActivityInterviewEditBinding by lazy {
        ActivityInterviewEditBinding.inflate(layoutInflater)
    }

    lateinit var interview: Interview
    private val database by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        interview = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("interview", Interview::class.java)
        } else {
            intent.getParcelableExtra("interview")
        } ?: throw IllegalArgumentException("No Interview entity passed to InterviewEditActivity")

        binding.fullName.setText(interview.name)
        binding.interviewer.setText(interview.interviewer)
        binding.location.setText(interview.location)

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

        binding.undo.setOnClickListener {
            finish()
        }

        binding.save.setOnClickListener {
            Thread {
                interview.name = binding.fullName.text.toString()
                interview.interviewer = binding.interviewer.text.toString()
                interview.location = binding.location.text.toString()

                database.interviewDao().update(interview)
                finish()
            }.start()
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