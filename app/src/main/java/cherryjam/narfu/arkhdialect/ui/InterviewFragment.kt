package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.adapter.InterviewAdapter
import cherryjam.narfu.arkhdialect.databinding.FragmentInterviewBinding
import cherryjam.narfu.arkhdialect.service.interview.FakerInterviewService
import cherryjam.narfu.arkhdialect.service.interview.InterviewService

class InterviewFragment : Fragment() {
    private val binding: FragmentInterviewBinding by lazy {
        FragmentInterviewBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: InterviewAdapter
    private val service: InterviewService = FakerInterviewService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(context, InterviewEditActivity::class.java)
            startActivity(intent)
        }

        adapter = InterviewAdapter()
        adapter.data = service.getData()

        binding.interviews.adapter = adapter
    }
}