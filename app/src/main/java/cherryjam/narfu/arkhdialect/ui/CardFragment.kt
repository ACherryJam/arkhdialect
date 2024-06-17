package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.adapter.CardAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.databinding.FragmentCardBinding

class CardFragment : Fragment() {
    private val binding: FragmentCardBinding by lazy {
        FragmentCardBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: CardAdapter
    private val database by lazy {
        AppDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsButton.setOnClickListener {
            Thread {
                val intent = Intent(context, SettingsActivity::class.java)
                startActivity(intent)
            }.start()
        }

        binding.floatingActionButton.setOnClickListener {
            Thread {
                val card = database.cardDao().insert(Card())

                val intent = Intent(context, CardEditActivity::class.java)
                intent.putExtra("card", card)
                startActivity(intent)
            }.start()
        }

        adapter = CardAdapter()
        AppDatabase.getInstance().cardDao().getAll().observe(viewLifecycleOwner) {
            adapter.data = it
        }
    }

    override fun onStart() {
        super.onStart()
        binding.cards.adapter = adapter
    }
}