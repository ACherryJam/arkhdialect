package cherryjam.narfu.arkhdialect.ui


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.databinding.ActivitySettingsBinding
import java.util.Locale
import cherryjam.narfu.arkhdialect.utils.ExportDataHelper


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
        val exportDataHelper = ExportDataHelper()
        binding.settingsExportHolder.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val database = AppDatabase.getInstance(this)

                //cards
                val cards = database.cardDao().getAll()
                exportDataHelper.exportToCSV(this, cards, "cards")

                //interviews
                val interviews = database.interviewDao().getAll()
                exportDataHelper.exportToCSV(this, interviews, "interview")
            }
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