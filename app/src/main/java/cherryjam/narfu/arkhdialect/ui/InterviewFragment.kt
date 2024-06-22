package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.SelectionTracker.SelectionObserver
import androidx.recyclerview.selection.StorageStrategy
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.interview.InterviewAdapter
import cherryjam.narfu.arkhdialect.adapter.interview.InterviewItemDetailsLookup
import cherryjam.narfu.arkhdialect.adapter.interview.InterviewItemKeyProvider
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
    private lateinit var selectionTracker: SelectionTracker<Long>

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
        binding.interviews.adapter = adapter

        selectionTracker = SelectionTracker.Builder(
            "interview",
            binding.interviews,
            InterviewItemKeyProvider(adapter),
            InterviewItemDetailsLookup(binding.interviews),
            StorageStrategy.createLongStorage()
        ).build()

        selectionTracker.addObserver(object : SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                val itemCount = selectionTracker.selection.size()
                if (!selectionTracker.hasSelection()) {
                    actionMode?.finish()
                    return
                }

                if (actionMode == null) {
                    requireActivity().startActionMode(actionModeCallback)
                }
                actionMode?.title = getString(R.string.selected_items, itemCount)
            }
        })
        adapter.tracker = selectionTracker

        database.interviewDao().getAll().observe(viewLifecycleOwner) {
            adapter.submitList(it)
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
                    adapter.submitList(it)
                }
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //selectionTracker.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        //selectionTracker.onRestoreInstanceState(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        actionMode?.finish()
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_select, menu)

            actionMode = mode
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialogHelper.showAlertDialog(
                        this@InterviewFragment.requireContext(),
                        title = getString(R.string.delete_interview_title),
                        message = getString(R.string.delete_interview_message, selectionTracker.selection.size()),
                        positiveText = getString(R.string.delete),
                        positiveCallback = {
                            deleteSelectedItems()
                            actionMode?.finish()
                        },
                        negativeText = getString(R.string.cancel),
                    )
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            selectionTracker.clearSelection()
        }
    }

    fun deleteSelectedItems() {
        Thread {
            for (id in selectionTracker.selection)
                database.interviewDao().deleteById(id)

            activity?.runOnUiThread { selectionTracker.clearSelection() }
        }.start()
    }



    companion object {
        val DELETE_INTERVIEW_ALERT = 1
    }
}