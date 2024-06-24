package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.InterviewAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.databinding.FragmentInterviewBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper

class InterviewFragment : Fragment() {
    private val binding: FragmentInterviewBinding by lazy {
        FragmentInterviewBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: InterviewAdapter
    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    private var actionMode: ActionMode? = null
    private lateinit var contextMenu: Menu

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
            Thread {
                val intent = Intent(context, SettingsActivity::class.java)
                startActivity(intent)
            }.start()
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
                if(query != null){
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

    private val selectableAdapterCallback = object : SelectableAdapter.Listener {
        override fun onSelectionStart() {
            actionMode = (activity as MainActivity).startSupportActionMode(actionModeCallback)
        }

        override fun onSelectionEnd() {
            actionMode?.finish()
        }

        override fun onSelectionChange() {
            actionMode?.title = getString(R.string.selected_items, adapter.selectedItemCount)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_select, menu)
            contextMenu = menu

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            binding.searchItem.visibility = View.GONE

            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialogHelper.showAlertDialog(
                        this@InterviewFragment.requireContext(),
                        title = getString(R.string.delete_selected_interview_title),
                        message = getString(R.string.delete_selected_interview_message, adapter.selectedItemCount),
                        positiveText = getString(R.string.delete),
                        positiveCallback = ::deleteSelectedItems,
                        negativeText = getString(R.string.cancel),
                    )
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            binding.searchItem.visibility = View.VISIBLE

            // Janky way to handle OnBackPressed in ActionMode
            // OnBackPressedCallback doesn't work
            if (adapter.isSelecting) {
                adapter.clearSelection()
                adapter.endSelection()
            }
        }
    }

    fun deleteSelectedItems() {
        Thread {
            for (position in adapter.getSelectedItemPositions())
                database.interviewDao().delete(adapter.data[position])

            activity?.runOnUiThread { adapter.endSelection() }
        }.start()
    }
}