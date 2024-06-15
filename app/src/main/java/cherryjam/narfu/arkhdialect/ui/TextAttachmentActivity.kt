package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.adapter.TextAttachmentAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityTextAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper

class TextAttachmentActivity : AppCompatActivity(), AlertDialogHelper.AlertDialogListener {
    private val binding: ActivityTextAttachmentBinding by lazy {
        ActivityTextAttachmentBinding.inflate(layoutInflater)
    }

    lateinit var adapter: TextAttachmentAdapter
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance() // Make sure to create instance first
    }

    private var actionMode: ActionMode? = null

    private lateinit var alertDialogHelper: AlertDialogHelper
    private lateinit var contextMenu: Menu

    lateinit var interview: Interview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        alertDialogHelper = AlertDialogHelper(this, this)

        interview = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("interview", Interview::class.java)
        } else {
            intent.getParcelableExtra("interview")
        } ?: throw IllegalArgumentException("No Interview entity passed to TextAttachmentActivity")

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = TextAttachmentAdapter(this)
        adapter.addListener(selectableAdapterCallback)
        database.textAttachmentDao().getByInterviewId(interview.id!!).observe(this) {
            adapter.data = it
        }

        binding.addTextAttachment.setOnClickListener {
            Thread {
                val textAttachment = AppDatabase.getInstance().textAttachmentDao().insert(TextAttachment(interview.id!!))

                val intent = Intent(this, TextAttachmentEditActivity::class.java)
                intent.putExtra("attachment", textAttachment)
                startActivity(intent)
            }.start()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.attachmentList.adapter = adapter
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private val selectableAdapterCallback = object : SelectableAdapter.Listener {
        override fun onSelectionStart() {
            actionMode = startSupportActionMode(actionModeCallback)
        }

        override fun onSelectionEnd() {
            actionMode?.finish()
        }

        override fun onItemSelect(position: Int) {
            actionMode?.title = getString(R.string.items_selected, adapter.getSelectedItemCount())

            val viewHolder = binding.attachmentList.findViewHolderForAdapterPosition(position)
                    as TextAttachmentAdapter.TextAttachmentViewHolder
            viewHolder.binding.listItem.setBackgroundResource(R.color.selected_item)
        }

        override fun onItemDeselect(position: Int) {
            actionMode?.title = getString(R.string.items_selected, adapter.getSelectedItemCount())

            val viewHolder = binding.attachmentList.findViewHolderForAdapterPosition(position)
                    as TextAttachmentAdapter.TextAttachmentViewHolder
            viewHolder.binding.listItem.setBackgroundResource(R.color.white)
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_select, menu)
            contextMenu = menu

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    alertDialogHelper.showAlertDialog(
                        getString(R.string.delete_interview_title),
                        getString(R.string.delete_interview_message, adapter.getSelectedItemCount()),
                        getString(R.string.delete_interview_positive),
                        getString(R.string.alert_negative),
                        InterviewFragment.DELETE_INTERVIEW_ALERT,
                        false
                    )
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null

            // Janky way to handle OnBackPressed in ActionMode
            // OnBackPressedCallback doesn't work
            if (adapter.isSelecting) {
                adapter.clearSelection()
                adapter.endSelection()
            }
        }
    }

    override fun onPositiveClick(from: Int) {
        Thread {
            for (position in adapter.getSelectedItemPositions()) {
                database.textAttachmentDao().delete(adapter.data[position])
            }

            runOnUiThread { adapter.endSelection() }
        }.start()
    }

    override fun onNegativeClick(from: Int) {
        TODO("Not yet implemented")
    }

    override fun onNeutralClick(from: Int) {
        TODO("Not yet implemented")
    }
}