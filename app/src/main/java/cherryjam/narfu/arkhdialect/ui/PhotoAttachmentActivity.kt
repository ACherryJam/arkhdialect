package cherryjam.narfu.arkhdialect.ui

import android.app.Activity
import android.content.ContentValues
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
import android.widget.Toast
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
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
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
            val photoUri: Uri? = try {
                createImageFile()  // Передаем context в createImageFile
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
                return
            }

            photoUri?.let { uri ->
                currentPhotoUri = uri
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)

                try {
                    cameraLauncher.launch(takePictureIntent)
                } catch (e: SecurityException) {
                    // Удаляем файл (по URI), если произошла ошибка доступа
                    contentResolver.delete(currentPhotoUri, null, null)
                    askForPermissionIfNeeded(mediaPermission)
                    askForPermissionIfNeeded(android.Manifest.permission.CAMERA)
                }
            } ?: run {
                Toast.makeText(this, "Не удалось создать URI для фотографии", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private lateinit var currentPhotoPath: String
    private lateinit var currentPhotoUri: Uri

    fun createFileName(fullName: String): String {
        val timestamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${fullName}_$timestamp"
    }


    private fun createImageFile(): Uri? {
        val folderName = interview.name.filter { c -> c.isLetterOrDigit() }.ifEmpty { "emptyName" }

        val imageFileName = createFileName(folderName) + "_"

        val timestamp: String = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        // ContentValues для хранения метаданных файла
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName + ".jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ArkhDialect" + "/$timestamp" + "/$folderName" + "/images")
        }

        // Вставка файла в хранилище через MediaStore
        val resolver = this.contentResolver
        val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            // Открываем поток для записи данных в файл
            resolver.openOutputStream(it)?.use { outputStream ->
                // Вы можете записывать данные изображения в outputStream, если они у вас есть
                // Например, если у вас есть Bitmap, вы можете его сохранить:
                // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            currentPhotoPath = it.toString() // Сохраняем путь к изображению как URI
        } ?: run {
            Toast.makeText(this, "Ошибка при создании изображения", Toast.LENGTH_SHORT).show()
        }

        return imageUri
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
                        message = getString(
                            R.string.delete_selected_photo_message,
                            adapter.selectedItemCount
                        ),
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