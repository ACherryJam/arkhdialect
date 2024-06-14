package cherryjam.narfu.arkhdialect.service.attachment

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cherryjam.narfu.arkhdialect.data.PhotoAttachment
import java.io.File

class MainPhotoAttachmentService(val context: Context) : PhotoAttachementService {
    private var attachments: MutableList<PhotoAttachment> = mutableListOf()
    override fun getData(): MutableList<PhotoAttachment> = attachments

    override fun updateAttachments() {
        val newAttachments: MutableList<PhotoAttachment> = mutableListOf()

        val projection = arrayOf( // media-database-columns-to-retrieve
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_MODIFIED
        )

        val selection = Bundle().apply {
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(MediaStore.Images.ImageColumns.DATE_MODIFIED))
        }

        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null
        )

        query?.use { cursor ->
            val idColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
            val dataColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA)

            while (cursor.moveToNext()) {
                val uri =  ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getLong(idColumn))
                val photoAttachment = PhotoAttachment(cursor.getLong(idColumn), uri)
                newAttachments.add(photoAttachment)
            }
        }

        attachments = newAttachments
    }
}