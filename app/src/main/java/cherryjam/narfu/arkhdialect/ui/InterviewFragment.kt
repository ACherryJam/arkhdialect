package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.InterviewAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.databinding.FragmentInterviewBinding
import cherryjam.narfu.arkhdialect.service.interview.FakerInterviewService
import cherryjam.narfu.arkhdialect.service.interview.InterviewService
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper.AlertDialogListener

class InterviewFragment : Fragment(), AlertDialogListener {
    private val binding: FragmentInterviewBinding by lazy {
        FragmentInterviewBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: InterviewAdapter
    private val service: InterviewService = FakerInterviewService()

    private var actionMode: ActionMode? = null
    public var isMultiSelect: Boolean = false

    private lateinit var alertDialogHelper: AlertDialogHelper
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

        alertDialogHelper = AlertDialogHelper(this)

        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(context, InterviewEditActivity::class.java)
            startActivity(intent)
        }

        adapter = InterviewAdapter()
        adapter.data = service.getData()
        adapter.addListener(object : SelectableAdapter.Listener {
            override fun onSelectionStart() {
                actionMode = (activity as MainActivity).startSupportActionMode(actionModeCallback)
            }

            override fun onSelectionEnd() {
                // Temporary removal solution
                // REPLACE THIS WITH SERVICE CALLS
                val dataToChange = adapter.data
                for (position in adapter.getSelectedItemPositions())
                    dataToChange.removeAt(position)
                adapter.data = dataToChange

                actionMode?.finish()
            }

            override fun onItemSelect(position: Int) {
                actionMode?.title = "Выбрано: ${adapter.getSelectedItemCount()}"

                val viewHolder = binding.interviews.findViewHolderForAdapterPosition(position)
                        as InterviewAdapter.InterviewViewHolder
                viewHolder.binding.listItem.setBackgroundResource(R.color.selected_item)
            }

            override fun onItemDeselect(position: Int) {
                actionMode?.title = "Выбрано: ${adapter.getSelectedItemCount()}"

                val viewHolder = binding.interviews.findViewHolderForAdapterPosition(position)
                        as InterviewAdapter.InterviewViewHolder
                viewHolder.binding.listItem.setBackgroundResource(R.color.white)
            }
        })

        binding.interviews.adapter = adapter
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
                    alertDialogHelper!!.showAlertDialog(
                        "",
                        "Delete Contact",
                        "DELETE",
                        "CANCEL",
                        1,
                    alertDialogHelper.showAlertDialog(
                        DELETE_INTERVIEW_ALERT,
                        false
                    )
                    return true
                }

                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            isMultiSelect = false
            binding.searchItem.visibility = View.VISIBLE
        }
    }

    override fun onPositiveClick(from: Int) {
        adapter.endSelection()
    }

    override fun onNegativeClick(from: Int) {}

    override fun onNeutralClick(from: Int) {}

    companion object {
        val DELETE_INTERVIEW_ALERT = 1
    }
}