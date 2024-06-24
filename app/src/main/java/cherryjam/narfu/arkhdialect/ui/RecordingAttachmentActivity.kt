package cherryjam.narfu.arkhdialect.ui

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.RecordingAttachmentAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.RecordingAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityRecordingAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var contextMenu: Menu

    private lateinit var adapter: RecordingAttachmentAdapter
    private val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

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
        val intent = Intent(Media.RECORD_SOUND_ACTION).apply {
            setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        // Recorder application developers tend to not add Media.RECORD_SOUND_ACTION to
        // their intent filter, Intent.resolveActivity(packageManager) is discarded in
        // favor of catching ActivityNotFoundException
        try {
            recorderLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(this, "Couldn't find a recorder app", Toast.LENGTH_SHORT).show()
        }
    }

    private val recorderLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            uri ?: return@registerForActivityResult

            try {
                saveRecordingAttachment(uri)
            }
            catch (e: IllegalArgumentException) {
                // Save threw because it couldn't read audio metadata columns
                // Creating new file using MediaStore API and copying data to it
                val copyUri = copyRecordingFile(uri)
                copyUri ?: return@registerForActivityResult

                // Finally saving recording attachment
                // Won't throw IllegalArgumentException because file's registered in MediaStore API
                saveRecordingAttachment(copyUri)
            }
        }
    }

    fun copyRecordingFile(receivedUri: Uri): Uri? {
        // 1. Determine directory for new file
        val audioCollection = Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val directory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Environment.DIRECTORY_RECORDINGS
        else
            Environment.DIRECTORY_MUSIC

        // 2. Create new file using content resolver
        val recordingDetails = ContentValues().apply {
            put(Media.RELATIVE_PATH, directory)
            put(Media.DISPLAY_NAME, createFileName())
            put(Media.MIME_TYPE, contentResolver.getType(receivedUri))
            put(Media.IS_PENDING, 1)
        }

        val audioUri = contentResolver.insert(audioCollection, recordingDetails)
        audioUri ?: return null

        // 3. Copy data from received file to the copy file
        val inputStream = contentResolver.openInputStream(receivedUri)
        val outputStream = contentResolver.openOutputStream(audioUri)

        if (inputStream == null || outputStream == null)
            return null
        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        // 4. Indicate that app finished writing the file
        recordingDetails.clear()
        recordingDetails.put(Media.IS_PENDING, 0)
        contentResolver.update(audioUri, recordingDetails, null, null)

        return audioUri
    }

    fun saveRecordingAttachment(uri: Uri) {
        contentResolver.query(
            uri,
            arrayOf(MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                    MediaStore.Audio.AudioColumns.DATE_ADDED,
                    MediaStore.Audio.AudioColumns.DURATION),
            null, null, null
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME)
            val timestampColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION)

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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}

    private val selectableAdapterCallback = object : SelectableAdapter.Listener {
        override fun onSelectionStart() {
            actionMode = startSupportActionMode(actionModeCallback)
        }

        override fun onSelectionEnd() {
            actionMode?.finish()
        }

        override fun onSelectionChange() {
            actionMode?.title = getString(R.string.selected_items, adapter.selectedItemCount)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_select, menu)
            contextMenu = menu

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            binding.toolbar.visibility = View.GONE
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialogHelper.showAlertDialog(
                        this@RecordingAttachmentActivity,
                        title = getString(R.string.delete_selected_recording_title),
                        message = getString(R.string.delete_selected_recording_message, adapter.selectedItemCount),
                        positiveText = getString(R.string.delete),
                        positiveCallback = ::deleteSelectedItems,
                        negativeText = getString(R.string.cancel),
                    )
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            binding.toolbar.visibility = View.VISIBLE

            // Janky way to handle OnBackPressed in ActionMode
            // OnBackPressedCallback doesn't work
            if (adapter.isSelecting) {
                adapter.clearSelection()
                adapter.endSelection()
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

    fun createFileName(): String {
        val fullName = interview.name.filter { c -> c.isLetterOrDigit() }
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        return "$fullName-$timestamp"
    }

    fun deleteOriginalFile(uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = MediaStore.createDeleteRequest(contentResolver, mutableListOf(uri))
            deleteFileLauncher.launch(IntentSenderRequest.Builder(intent.intentSender).build())
        }
    }

    val deleteFileLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("deleteFileLauncher", "Android 11 or higher : deleted")
        }
    }

    companion object {
        private const val REQUEST_CODE = 1
    }
}