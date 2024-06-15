package cherryjam.narfu.arkhdialect.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Audio.Media
import androidx.appcompat.app.AppCompatActivity
import cherryjam.narfu.arkhdialect.adapter.RecordingAttachmentAdapter
import cherryjam.narfu.arkhdialect.databinding.ActivityRecordingAttachmentBinding
import cherryjam.narfu.arkhdialect.service.attachment.IRecordingAttachmentService
import cherryjam.narfu.arkhdialect.service.attachment.RecordingAttachmentService
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class RecordingAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityRecordingAttachmentBinding by lazy {
        ActivityRecordingAttachmentBinding.inflate(layoutInflater)
    }

    private lateinit var service: IRecordingAttachmentService
    private lateinit var adapter: RecordingAttachmentAdapter
    private val permissions = arrayOf(mediaPermission,Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        // to show all recordings
        service = RecordingAttachmentService(this)
        service.updateAttachments()

        adapter = RecordingAttachmentAdapter(this)
        adapter.data = service.getData()
        binding.attachmentList.adapter = adapter

        binding.addRecordingAttachment.setOnClickListener {
            startRecording()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200) {
            var allPermissionsGranted = true
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false
                    break
                }
            }
            if (allPermissionsGranted) {
                startRecording()
            } else {
                Snackbar.make(
                    binding.root,
                    "Can't get data without permission",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startRecording() {
        val intent = Intent(Media.RECORD_SOUND_ACTION)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val audioUri: Uri? = data?.data
            audioUri?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val outputFile = createRecordingFile()
                    inputStream?.use { input ->
                        outputFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Now the file is saved in the custom directory
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
    @Throws(IOException::class)
    private fun createRecordingFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val recording: File = File.createTempFile("RECORD_${timeStamp}_", ".mp3", storageDir)

        return recording
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