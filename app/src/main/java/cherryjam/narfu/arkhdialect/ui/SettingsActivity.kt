package cherryjam.narfu.arkhdialect.ui


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.databinding.ActivitySettingsBinding


class SettingsActivity : AppCompatActivity() {
    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

//    lateinit var radio_group_dialog: RadioGroupDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.general_settings, GeneralSettingsFragment())
                .commit()
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.settingsAboutHolder.setOnClickListener {
            Thread {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }.start()
        }

        binding.settingsRateHolder.setOnClickListener {
            openUrl("https://docs.google.com/forms/d/e/1FAIpQLScbRqBZ8W7_E-pSyl3NJoz87Tyr2ZsBM7ENo7elJiUpMEIC1Q/viewform?usp=sf_link")
        }

    }


    class GeneralSettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.general_preferences, rootKey)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
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