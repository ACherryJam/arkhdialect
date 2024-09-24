package cherryjam.narfu.arkhdialect.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.dao.TextAttachmentDao
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.primaryConstructor
import cherryjam.narfu.arkhdialect.data.dao.InterviewDao
import java.io.BufferedOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


class ExportDataHelper(val database: AppDatabase, val context: Context) {
    private lateinit var interviewDatabase: InterviewDao
    private lateinit var textDatabase: TextAttachmentDao

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun exportToTXT() {
        interviewDatabase = database.interviewDao()
        textDatabase = database.textAttachmentDao()

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dataString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val interviews = interviewDatabase.getAll()

        interviews.observe(context as LifecycleOwner) { list_interviews ->
            // Проверяем, пустой ли список интервью
            if (list_interviews.isNullOrEmpty()) {
//                Toast.makeText(context, "No interviews available", Toast.LENGTH_SHORT).show()
                return@observe
            }

            try {
                list_interviews.forEach { interview ->
                    val folderName = interview.name.filter { c -> c.isLetterOrDigit() }.ifEmpty { "emptyName" }
                    val data = textDatabase.getByInterviewId(interview.id)

                    data.observe(context as LifecycleOwner) { list ->
                        // Проверяем, пустой ли список данных
                        if (list.isNullOrEmpty()) {
//                            Toast.makeText(context, "No text data for interview: ${interview.name}", Toast.LENGTH_SHORT).show()
                            return@observe
                        }

                        // Задаем путь сохранения файла
                        val basePath = "/storage/emulated/0/Documents"
                        val fullPath = "$basePath/ArkhDialect_notes/$dataString/notes/${interview.id}_$folderName"

                        // Создаем папки, если они не существуют
                        val dir = File(fullPath)
                        if (!dir.exists()) {
                            val success = dir.mkdirs()
                            if (!success) {
//                                Toast.makeText(context, "Failed to create directory for TextAttachment: $fullPath", Toast.LENGTH_SHORT).show()
                                return@observe
                            }
                        }

                        // Формируем имя файла
                        val filename = "${list[0].title.filter { it.isLetterOrDigit() }}_$timestamp.txt"
                        val file = File(dir, filename)

                        try {
                            // Пишем данные в файл
                            BufferedWriter(FileWriter(file, Charsets.UTF_8)).use { writer ->
                                val header = list[0].title
                                val content = list[0].content

                                writer.write("$header\n")
                                writer.write("$content\n")
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
//                            Toast.makeText(context, "Error writing txt file: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
//                Toast.makeText(context, "An error occurred during export txt file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun <T : Any> exportToCSV(data: LiveData<List<T>>, fileNamePrefix: String) {
        // Форматируем дату
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val dataString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())

        data.observe(context as LifecycleOwner) { list ->
            try {
                val basePath = "/storage/emulated/0/Documents"
                val fullPath = "$basePath/ArkhDialect_CSV/$dataString/CSV/"
                val filename = "${fileNamePrefix}_$timestamp.csv"

                // Создаем папки, если они не существуют
                val dir = File(fullPath)
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(fullPath, filename)

                BufferedWriter(FileWriter(file, Charsets.UTF_8)).use { writer ->
                    // Если список не пуст, получаем список полей динамически
                    if (list.isNotEmpty()) {
                        val fields =
                            list.first()::class.primaryConstructor!!.parameters.map { it.name }

                        // Пишем заголовок (имена полей)
                        if (fileNamePrefix == "cards") {
                            val header = arrayOf(
                                "Слово",
                                "Район",
                                "Грамматика",
                                "Значение",
                                "Примеры"
                            ).joinToString(separator = ";")
                            writer.write("$header\n")
                        } else {
                            val header =
                                arrayOf("ФИО", "Интервьюер", "Район").joinToString(separator = ";")
                            writer.write("$header\n")
                        }

                        // Пишем строки данных
                        list.forEach { item ->
                            val values = fields.drop(1).map { field ->
                                val property = item::class.members.find { it.name == field }
                                property?.call(item)?.toString().orEmpty()
                            }
                            val resultString =
                                values.joinToString(separator = ";").replace("\n", " ")
                            writer.write(resultString + "\n")
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun zipFolders() {
        // directories
        val basePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath

        val fileCSV = File(basePath, "ArkhDialect_CSV")
        val fileNotes = File(basePath, "ArkhDialect_notes")
        val filePictures = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ArkhDialect_pictures")
        val fileRecordings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RECORDINGS), "ArkhDialect_recordings")
        } else {
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "ArkhDialect_recordings")
        }

        // Проверяем существование директорий
        val sourceFolders = listOf(fileCSV, fileNotes, filePictures, fileRecordings).filter { it.exists() }

        val zipPath = "$basePath/ArkhDialect_data"
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Создаем папку, если она не существует
        val dir = File(zipPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val zipFile = File(zipPath, "${timestamp}_data.zip")

        try {
            val fos = FileOutputStream(zipFile)
            val zos = ZipOutputStream(BufferedOutputStream(fos))

            sourceFolders.forEach { folder ->
                zipFolder(folder, folder.name, zos)
            }

            zos.close()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
            println("Failed to zip files")
        }
    }

    private fun zipFolder(folder: File, parentFolder: String, zos: ZipOutputStream) {
        val files = folder.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                zipFolder(file, "$parentFolder/${file.name}", zos)
            } else {
                FileInputStream(file).use { fis ->
                    val entry = ZipEntry("$parentFolder/${file.name}")
                    zos.putNextEntry(entry)

                    fis.copyTo(zos, 1024)
                    zos.closeEntry()
                }
            }
        }
    }

}