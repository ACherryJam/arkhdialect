package cherryjam.narfu.arkhdialect.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cherryjam.narfu.arkhdialect.adapter.RecordingAttachmentAdapter
import cherryjam.narfu.arkhdialect.databinding.ActivityRecordingAttachmentBinding
import cherryjam.narfu.arkhdialect.service.attachment.IRecordingAttachmentService
import cherryjam.narfu.arkhdialect.service.attachment.RecordingAttachmentService
import com.google.android.material.snackbar.Snackbar
import java.io.IOException

class RecordingAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityRecordingAttachmentBinding by lazy {
        ActivityRecordingAttachmentBinding.inflate(layoutInflater)
    }

    lateinit var service: IRecordingAttachmentService
    lateinit var adapter: RecordingAttachmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        askForPermissionIfNeeded(mediaPermission)

        service = RecordingAttachmentService(this)
        service.updateAttachments()

        adapter = RecordingAttachmentAdapter(this)
        adapter.data = service.getData()
        binding.attachmentList.adapter = adapter

        binding.addRecordingAttachment.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                askForPermissionIfNeeded(Manifest.permission.RECORD_AUDIO)
            } else {
                openRecorderIntent()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun askForPermissionIfNeeded(permission: String) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(permission), REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(
                binding.root,
                "Can't get data without permission",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

        }
    }

    private fun openRecorderIntent() {
        val intent = Intent(Media.RECORD_SOUND_ACTION)
        startForResult.launch(intent)

        /*
        if (intent.resolveActivity(packageManager) != null) {
            startForResult.launch(intent)
        } else {
            // Обработка случая, когда нет приложения для записи аудио
            Toast.makeText(this, "No audio recording app found", Toast.LENGTH_SHORT).show()
        }
        */
    }



    companion object {
        private val mediaPermission: String by lazy {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
        }
        private const val REQUEST_CODE = 1
    }
}