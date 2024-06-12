package cherryjam.narfu.arkhdialect.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import cherryjam.narfu.arkhdialect.adapter.PhotoAttachmentAdapter
import cherryjam.narfu.arkhdialect.databinding.ActivityPhotoAttachmentBinding
import cherryjam.narfu.arkhdialect.service.attachment.MainPhotoAttachmentService
import cherryjam.narfu.arkhdialect.service.attachment.PhotoAttachementService
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

    lateinit var service: PhotoAttachementService
    private lateinit var adapter: PhotoAttachmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        askForPermissionIfNeeded(mediaPermission)
        askForPermissionIfNeeded(android.Manifest.permission.CAMERA)

        binding.addPhotoAttachment.setOnClickListener {
            openCameraIntent()
        }

        service = MainPhotoAttachmentService(this)
        service.updateAttachments()

        adapter = PhotoAttachmentAdapter(this)
        adapter.data = service.getData()
        binding.attachmentList.adapter = adapter
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
                service.updateAttachments()
                runOnUiThread {
                    adapter.data = service.getData()
                }
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

            val photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraLauncher.launch(takePictureIntent)
        }
    }

    private lateinit var currentPhotoPath: String

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir: File? = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image: File = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = image.absolutePath
        return image
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