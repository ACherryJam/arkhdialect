package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.adapter.CardAdapter
import cherryjam.narfu.arkhdialect.databinding.FragmentCardBinding
import cherryjam.narfu.arkhdialect.service.card.CardService
import cherryjam.narfu.arkhdialect.service.card.FakerCardService

class CardFragment : Fragment() {
    private val binding: FragmentCardBinding by lazy {
        FragmentCardBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: CardAdapter
    private val service: CardService = FakerCardService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(context, CardEditActivity::class.java)
            startActivity(intent)
        }

        adapter = CardAdapter()
        adapter.data = service.getData()

        binding.cards.adapter = adapter
    }
}