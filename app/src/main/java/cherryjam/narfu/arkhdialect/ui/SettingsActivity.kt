package cherryjam.narfu.arkhdialect.ui


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.ActivitySettingsBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper
import java.util.Locale
import cherryjam.narfu.arkhdialect.utils.ExportDataHelper
import java.io.IOException


class SettingsActivity : AppCompatActivity() {
    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.settingsLanguage.text = Locale.getDefault().displayLanguage
        binding.settingsLanguageHolder.setOnClickListener {
            launchChangeAppLanguageIntent()
        }

        binding.settingsAboutHolder.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        binding.settingsRateHolder.setOnClickListener {
            openUrl("https://forms.yandex.ru/cloud/66eff991eb6146b6ea4f2be5/")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.settingsExportHolder.setOnClickListener {
                AlertDialogHelper.showAlertDialog(
                    this@SettingsActivity,
                    title = getString(R.string.export_data_title),
                    message = getString(R.string.export_data_message),
                    positiveText = getString(R.string.export_data),
                    positiveCallback = ::launchExportData,
                    negativeText = getString(R.string.cancel),
                )
            }
        }
        else {
            binding.settingsExportLabel.setTextColor(ContextCompat.getColor(this, R.color.gray))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun launchChangeAppLanguageIntent() {
        try {
            val action = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Settings.ACTION_APP_LOCALE_SETTINGS
            else
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS

            val intent = Intent(action).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, this.getString(R.string.no_settings_found), Toast.LENGTH_SHORT).show()
        }
    }

    fun launchExportData() {
        val database = AppDatabase.getInstance(this)
        val exportDataHelper = ExportDataHelper(database, this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                //cards
                val cards = database.cardDao().getAll()
                exportDataHelper.exportToCSV(cards, "cards")

                //interviews
                val interviews = database.interviewDao().getAll()
                exportDataHelper.exportToCSV(interviews, "interview")

                //textAttachments
                exportDataHelper.exportToTXT()

                // zip file
                exportDataHelper.zipFolders()

                Toast.makeText(this, this.getString(R.string.export_successful), Toast.LENGTH_SHORT).show()
                Toast.makeText(this, this.getString(R.string.export_directory), Toast.LENGTH_SHORT).show()
                //TODO
                //Добавить кнопку для выбора сохранения в проводнике

            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, this.getString(R.string.export_unsuccessful), Toast.LENGTH_SHORT).show()
            }
        }
        else {
            Toast.makeText(this, this.getString(R.string.export_unsuccessful), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}