package cherryjam.narfu.arkhdialect.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import cherryjam.narfu.arkhdialect.adapter.RecordingAttachmentAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityRecordingAttachmentBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecordingAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityRecordingAttachmentBinding by lazy {
        ActivityRecordingAttachmentBinding.inflate(layoutInflater)
    }

    lateinit var interview: Interview
    private lateinit var adapter: RecordingAttachmentAdapter
    private val permissions = arrayOf(mediaPermission,Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        interview = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("interview", Interview::class.java)
        } else {
            intent.getParcelableExtra("interview")
        } ?: throw IllegalArgumentException("No Interview entity passed to RecordingAttachmentActivity")

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)

        adapter = RecordingAttachmentAdapter(this)
        AppDatabase.getInstance().recordingAttachmentDao().getByInterviewId(interview.id!!).observe(this) {
            adapter.data = it
        }

        binding.addRecordingAttachment.setOnClickListener {
            startRecording()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.attachmentList.adapter = adapter
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
            val recordingFile: File = try { createRecordingFile() } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            val uri = FileProvider.getUriForFile(this, "$packageName.provider", recordingFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            recorderLauncher.launch(intent)
        }
    }

    private val recorderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri?.let {
                contentResolver.query(
                    it,
                    arrayOf(Media.DISPLAY_NAME, Media.DATE_ADDED, Media.DURATION),
                    null, null, null
                )
            }?.use { cursor ->
                val nameColumn = cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME)
                val timestampColumn = cursor.getColumnIndexOrThrow(Media.DATE_ADDED)
                val durationColumn = cursor.getColumnIndexOrThrow(Media.DURATION)

                if (cursor.moveToFirst()) {
                    val name = cursor.getString(nameColumn)
                    val timestamp = cursor.getInt(timestampColumn)
                    val duration = cursor.getInt(durationColumn)

                    Thread {
                        AppDatabase.getInstance().recordingAttachmentDao().insert(RecordingAttachment(
                            interview.id!!, uri, name, timestamp, duration
                        ))
                    }.start()
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