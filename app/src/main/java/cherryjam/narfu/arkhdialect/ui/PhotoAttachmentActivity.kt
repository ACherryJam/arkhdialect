package cherryjam.narfu.arkhdialect.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.PhotoAttachmentAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.PhotoAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityPhotoAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityPhotoAttachmentBinding by lazy {
        ActivityPhotoAttachmentBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: PhotoAttachmentAdapter

    lateinit var interview: Interview
    private val database by lazy { AppDatabase.getInstance(this) }

    private var actionMode: ActionMode? = null
    private lateinit var contextMenu: Menu

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
        } ?: throw IllegalArgumentException("No Interview entity passed to PhotoAttachmentActivity")

        askForPermissionIfNeeded(mediaPermission)
        askForPermissionIfNeeded(android.Manifest.permission.CAMERA)

        binding.addPhotoAttachment.setOnClickListener {
            openCameraIntent()
        }

        adapter = PhotoAttachmentAdapter()
        adapter.addListener(selectableAdapterCallback)
        database.photoAttachmentDao().getByInterviewId(interview.id!!).observe(this) {
            adapter.data = it
        }
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
                "Can't take and get images without permission",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val callback = OnScanCompletedListener { _, _ ->
                val attachment = PhotoAttachment(interview.id!!, currentPhotoUri)
                database.photoAttachmentDao().insert(attachment)
            }

            MediaScannerConnection.scanFile(
                this,
                arrayOf(currentPhotoPath),
                null, callback
            )
        }
    }

    private fun openCameraIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File = try { createImageFile() } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            currentPhotoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            try {
                cameraLauncher.launch(takePictureIntent)
            } catch (e: SecurityException) {
                photoFile.delete()
                askForPermissionIfNeeded(mediaPermission)
                askForPermissionIfNeeded(android.Manifest.permission.CAMERA)
            }
        }
    }

    private lateinit var currentPhotoPath: String
    private lateinit var currentPhotoUri: Uri

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir: File? = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image: File = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
    }

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
                        this@PhotoAttachmentActivity,
                        title = getString(R.string.delete_selected_photo_title),
                        message = getString(R.string.delete_selected_photo_message, adapter.selectedItemCount),
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
            for (position in adapter.getSelectedItemPositions())
                database.photoAttachmentDao().delete(adapter.data[position])

            runOnUiThread { adapter.endSelection() }
        }.start()
    }

    companion object {
        private val mediaPermission: String by lazy {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                android.Manifest.permission.READ_MEDIA_IMAGES
            else
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        private const val REQUEST_CODE = 1
    }
}