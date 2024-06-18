package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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

class TextAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityTextAttachmentBinding by lazy {
        ActivityTextAttachmentBinding.inflate(layoutInflater)
    }

    lateinit var adapter: TextAttachmentAdapter
    private val database by lazy { AppDatabase.getInstance(this) }

    private var actionMode: ActionMode? = null
    private lateinit var contextMenu: Menu

    lateinit var interview: Interview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        interview = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("interview", Interview::class.java)
        } else {
            intent.getParcelableExtra("interview")
        } ?: throw IllegalArgumentException("No Interview entity passed to TextAttachmentActivity")

        adapter = TextAttachmentAdapter(this)
        adapter.addListener(selectableAdapterCallback)
        database.textAttachmentDao().getByInterviewId(interview.id!!).observe(this) {
            adapter.data = it
        }

        binding.addTextAttachment.setOnClickListener {
            Thread {
//                val textAttachment = database.textAttachmentDao().insert(TextAttachment(interview.id!!))
                val textAttachment = TextAttachment(interview.id!!)

                val intent = Intent(this, TextAttachmentEditActivity::class.java)
                intent.putExtra("attachment", textAttachment)
                intent.putExtra("new", true)
                startActivity(intent)
            }.start()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.attachmentList.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        actionMode?.finish()
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
            actionMode?.title = getString(R.string.selected_items, adapter.getSelectedItemCount())
        }

        override fun onItemDeselect(position: Int) {
            actionMode?.title = getString(R.string.selected_items, adapter.getSelectedItemCount())
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_select, menu)
            contextMenu = menu

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu?): Boolean {
            binding.toolbar.visibility = View.GONE
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialogHelper.showAlertDialog(
                        this@TextAttachmentActivity,
                        title = getString(R.string.delete_text_title),
                        message = getString(R.string.delete_text_message, adapter.getSelectedItemCount()),
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
            binding.toolbar.visibility = View.VISIBLE

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
            for (position in adapter.getSelectedItemPositions()) {
                database.textAttachmentDao().delete(adapter.data[position])
            }

            runOnUiThread { adapter.endSelection() }
        }.start()
    }
}