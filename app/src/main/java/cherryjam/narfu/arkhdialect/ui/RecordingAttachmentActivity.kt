package cherryjam.narfu.arkhdialect.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore.Audio.Media
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider

class RecordingAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityRecordingAttachmentBinding by lazy {
        ActivityRecordingAttachmentBinding.inflate(layoutInflater)
    }

    private lateinit var service: IRecordingAttachmentService
    private lateinit var adapter: RecordingAttachmentAdapter
    private lateinit var currentRecordingPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val permissions = arrayOf(mediaPermission)
        askForPermissionIfNeeded(permissions)

        // to show all recordings
        service = RecordingAttachmentService(this)
        service.updateAttachments()

        adapter = RecordingAttachmentAdapter(this)
        adapter.data = service.getData()
        binding.attachmentList.adapter = adapter

        binding.addRecordingAttachment.setOnClickListener {
            openRecorderIntent()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }


    private fun askForPermissionIfNeeded(permissions: Array<String>) {
        if (!hasPermissions(this, permissions)) {
            requestPermissions(permissions, REQUEST_CODE)
        }
    }

    private fun hasPermissions(attachment: RecordingAttachmentActivity, permissions: Array<String>): Boolean {
        for(permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false
        }
        return true
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

    private val recorderLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val callback = MediaScannerConnection.OnScanCompletedListener { _, _ ->
                service.updateAttachments()
                runOnUiThread {
                    adapter.data = service.getData()
                }
            }

            MediaScannerConnection.scanFile(
                this,
                arrayOf(currentRecordingPath),
                null, callback
            )

        }
    }

    private fun openRecorderIntent() {
        val recordIntent = Intent(Media.RECORD_SOUND_ACTION)

        val recordingFile: File = try { createRecordingFile() } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        val recordingUri = FileProvider.getUriForFile(this, "$packageName.provider", recordingFile)
        recordIntent.putExtra(MediaStore.EXTRA_OUTPUT, recordingUri)

        recorderLauncher.launch(recordIntent)

        /*
        if (intent.resolveActivity(packageManager) != null) {
            startForResult.launch(intent)
        } else {
            // Обработка случая, когда нет приложения для записи аудио
            Toast.makeText(this, "No audio recording app found", Toast.LENGTH_SHORT).show()
        }
        */
    }

    private fun createRecordingFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val recordingFileName = "record_" + timeStamp + "_"
        val storageDir: File? = getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val recording: File = File.createTempFile(recordingFileName, ".m4a", storageDir)

        currentRecordingPath = recording.absolutePath

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