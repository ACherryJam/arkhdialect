package cherryjam.narfu.arkhdialect.service.attachment

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import cherryjam.narfu.arkhdialect.data.RecordingAttachment

class RecordingAttachmentService(val context: Context) : IRecordingAttachmentService {
    private var attachments: MutableList<RecordingAttachment> = mutableListOf()
    override fun getData(): MutableList<RecordingAttachment> = attachments
    override fun updateAttachments() {
        val audioList: MutableList<RecordingAttachment> = mutableListOf()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val projection = arrayOf(
            Media._ID,
            Media.DISPLAY_NAME,
            Media.DATE_ADDED,
            Media.DURATION
        )

        val selection = Bundle().apply {
            //putInt(ContentResolver.QUERY_ARG_LIMIT, 20)
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
            putStringArray(
                ContentResolver.QUERY_ARG_SORT_COLUMNS,
                arrayOf(Media.DATE_ADDED)
            )
        }

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            null
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(Media.DISPLAY_NAME)
            val timestampColumn = cursor.getColumnIndexOrThrow(Media.DATE_ADDED)
            val durationColumn = cursor.getColumnIndexOrThrow(Media.DURATION)

            while (cursor.moveToNext()) {
                // Get values of columns for a given audio.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val uri = ContentUris.withAppendedId(Media.EXTERNAL_CONTENT_URI, id)
                val timeStamp = cursor.getInt(timestampColumn)
                val duration = cursor.getInt(durationColumn)
                val recordAttachment = RecordingAttachment(id, name, uri, timeStamp, duration)
                audioList.add(recordAttachment)
            }
        }
        attachments = audioList
}
    }