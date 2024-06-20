package cherryjam.narfu.arkhdialect.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import cherryjam.narfu.arkhdialect.adapter.RecordingAttachmentAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityRecordingAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.SelectableHelper
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class RecordingAttachmentActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private val binding: ActivityRecordingAttachmentBinding by lazy {
        ActivityRecordingAttachmentBinding.inflate(layoutInflater)
    }

    lateinit var interview: Interview
    private val database by lazy { AppDatabase.getInstance(this) }

    private var actionMode: ActionMode? = null

    private lateinit var selectableHelper: SelectableHelper<RecordingAttachmentAdapter.RecordingAttachmentViewHolder>
    private lateinit var selectableAdapterCallback: SelectableAdapter.Listener

    private lateinit var adapter: RecordingAttachmentAdapter
    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var extention: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val extensions = sharedPreferences!!.getString("formats", "1")

        when(extensions) {
            "1" -> extention = ".mp3"
            "2" -> extention = ".m4a"
            "3" -> extention = ".ogg"
        }

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
        selectableHelper = SelectableHelper(null, this, adapter, this, ::deleteSelectedItems, ::checkShowItem)
        selectableAdapterCallback = selectableHelper.getSelectableAdapterCallback()

        adapter.addListener(selectableAdapterCallback)
        database.recordingAttachmentDao().getByInterviewId(interview.id!!).observe(this) {
            adapter.data = it
        }

        binding.addRecordingAttachment.setOnClickListener {
            startRecording()
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        binding.attachmentList.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        actionMode?.finish()
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

        // Recorder application developers tend to not add Media.RECORD_SOUND_ACTION to
        // their intent filter, Intent.resolveActivity(packageManager) is discarded in
        // favor of catching ActivityNotFoundException
        try {
            val recordingFile: File = try { createRecordingFile() } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            val uri = FileProvider.getUriForFile(this, "$packageName.provider", recordingFile)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            recorderLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "Couldn't find a recorder app", Toast.LENGTH_SHORT).show()
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
                        database.recordingAttachmentDao().insert(RecordingAttachment(
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
        //val recording: File = File.createTempFile("RECORD_${timeStamp}_", ".mp3", storageDir)
        val recording: File = File.createTempFile("RECORD_${timeStamp}_", extention, storageDir)

        return recording
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "formats" -> {
                val format = sharedPreferences!!.getString("formats", "1")

                when(format) {
                    "1" -> extention = ".mp3"
                    "2" -> extention = ".m4a"
                    "3" -> extention = ".ogg"
                }
            }

        }
    }

    fun deleteSelectedItems() {
        Thread {
            for (position in adapter.getSelectedItemPositions()) {
                database.recordingAttachmentDao().delete(adapter.data[position])
            }

            runOnUiThread { adapter.endSelection() }
        }.start()
    }

    companion object {
        private val mediaPermission: String by lazy {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_AUDIO
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
        }
        private const val REQUEST_CODE = 1
    }

    fun checkShowItem() {
        if (selectableHelper.flag)
            binding.toolbar.visibility = View.VISIBLE
        else
            binding.toolbar.visibility = View.GONE
    }
}