package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.adapter.InterviewAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.FragmentInterviewBinding
import cherryjam.narfu.arkhdialect.utils.SelectableHelper

class InterviewFragment : Fragment() {
    private val binding: FragmentInterviewBinding by lazy {
        FragmentInterviewBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: InterviewAdapter
    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    private var actionMode: ActionMode? = null

    private lateinit var selectableHelper: SelectableHelper<InterviewAdapter.InterviewViewHolder>
    private lateinit var selectableAdapterCallback: SelectableAdapter.Listener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.settingsButton.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.floatingActionButton.setOnClickListener {
            Thread {
                val interview = database.interviewDao().insert(Interview())

                val intent = Intent(context, InterviewEditActivity::class.java)
                intent.putExtra("interview", interview)
                startActivity(intent)
            }.start()
        }

        adapter = InterviewAdapter()
        selectableHelper = SelectableHelper(this, null, adapter, requireContext(), ::deleteSelectedItems, ::checkShowItem)
        selectableAdapterCallback = selectableHelper.getSelectableAdapterCallback()

        adapter.addListener(selectableAdapterCallback)
        database.interviewDao().getAll().observe(viewLifecycleOwner) {
            adapter.data = it
        }


        val searchView = binding.searchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    searchDatabase(query)
                }
                return true
            }

            private fun searchDatabase(query: String) {
                val searchQuery = "%$query%"

                database.interviewDao().searchDatabase(searchQuery).observe(viewLifecycleOwner) {
                    adapter.data = it
                }
            }
        })


    }

    override fun onStart() {
        super.onStart()
        binding.interviews.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        actionMode?.finish()
    }

    fun deleteSelectedItems() {
        Thread {
            for (position in adapter.getSelectedItemPositions())
                database.interviewDao().delete(adapter.data[position])

            activity?.runOnUiThread { adapter.endSelection() }
        }.start()
    }

    fun checkShowItem() {
        if (selectableHelper.flag)
            binding.searchItem.visibility = View.VISIBLE
        else
            binding.searchItem.visibility = View.GONE
    }

    companion object {
        val DELETE_INTERVIEW_ALERT = 1
    }
}