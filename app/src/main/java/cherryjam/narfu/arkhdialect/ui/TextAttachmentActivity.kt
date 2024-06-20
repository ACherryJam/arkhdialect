package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.adapter.TextAttachmentAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Interview
import cherryjam.narfu.arkhdialect.data.entity.TextAttachment
import cherryjam.narfu.arkhdialect.databinding.ActivityTextAttachmentBinding
import cherryjam.narfu.arkhdialect.utils.SelectableHelper

class TextAttachmentActivity : AppCompatActivity() {
    private val binding: ActivityTextAttachmentBinding by lazy {
        ActivityTextAttachmentBinding.inflate(layoutInflater)
    }

    lateinit var adapter: TextAttachmentAdapter
    private val database by lazy { AppDatabase.getInstance(this) }

    private var actionMode: ActionMode? = null

    private lateinit var selectableHelper: SelectableHelper<TextAttachmentAdapter.TextAttachmentViewHolder>
    private lateinit var selectableAdapterCallback: SelectableAdapter.Listener

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
        selectableHelper = SelectableHelper(null, this, adapter, this, ::deleteSelectedItems, ::checkShowItem)
        selectableAdapterCallback = selectableHelper.getSelectableAdapterCallback()

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


    fun deleteSelectedItems() {
        Thread {
            for (position in adapter.getSelectedItemPositions()) {
                database.textAttachmentDao().delete(adapter.data[position])
            }

            runOnUiThread { adapter.endSelection() }
        }.start()
    }

    fun checkShowItem() {
        if (selectableHelper.flag)
            binding.toolbar.visibility = View.VISIBLE
        else
            binding.toolbar.visibility = View.GONE
    }
}