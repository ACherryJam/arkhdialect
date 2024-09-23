package cherryjam.narfu.arkhdialect.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.primaryConstructor


class ExportDataHelper() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun <T : Any> exportToCSV(context: Context, data: LiveData<List<T>>, fileNamePrefix: String) {
        // Форматируем дату
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        data.observe(context as LifecycleOwner) { list ->
            try {
                val filePath = context.getExternalFilesDir(null)?.absolutePath
                val filename = "${fileNamePrefix}_$dateString.csv"
                val file = File(filePath, filename)

                BufferedWriter(FileWriter(file, Charsets.UTF_8)).use { writer ->
                    // Если список не пуст, получаем список полей динамически
                    if (list.isNotEmpty()) {
                        val fields = list.first()::class.primaryConstructor!!.parameters.map { it.name }

                        // Пишем заголовок (имена полей)
                        if (fileNamePrefix == "cards"){
                            val header = arrayOf("Слово", "Район", "Грамматика", "Значение", "Примеры").joinToString(separator = ";")
                            writer.write("$header\n")
                        }
                        else {
                            val header = arrayOf("ФИО", "Интервьюер", "Район").joinToString(separator = ";")
                            writer.write("$header\n")
                        }


                        // Пишем строки данных
                        list.forEach { item ->
                            val values = fields.drop(1).map { field ->
                                val property = item::class.members.find { it.name == field }
                                property?.call(item)?.toString().orEmpty()
                            }
                            val resultString = values.joinToString(separator = ";").replace("\n", " ")
                            writer.write(resultString + "\n")
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}