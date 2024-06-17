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
import cherryjam.narfu.arkhdialect.adapter.CardAdapter
import cherryjam.narfu.arkhdialect.adapter.SelectableAdapter
import cherryjam.narfu.arkhdialect.data.AppDatabase
import cherryjam.narfu.arkhdialect.data.entity.Card
import cherryjam.narfu.arkhdialect.databinding.FragmentCardBinding
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper

class CardFragment : Fragment() {
    private val binding: FragmentCardBinding by lazy {
        FragmentCardBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: CardAdapter
    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    private var actionMode: ActionMode? = null
    private lateinit var contextMenu: Menu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.floatingActionButton.setOnClickListener {
            Thread {
                val card = database.cardDao().insert(Card())

                val intent = Intent(context, CardEditActivity::class.java)
                intent.putExtra("card", card)
                startActivity(intent)
            }.start()
        }

        adapter = CardAdapter()
        adapter.addListener(selectableAdapterCallback)
        database.cardDao().getAll().observe(viewLifecycleOwner) {
            adapter.data = it
        }
    }

    override fun onStart() {
        super.onStart()
        binding.cards.adapter = adapter
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
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.action_delete -> {
                    AlertDialogHelper.showAlertDialog(
                        this@CardFragment.requireContext(),
                        title = getString(R.string.delete_card_title),
                        message = getString(R.string.delete_card_message, adapter.getSelectedItemCount()),
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
                database.cardDao().delete(adapter.data[position])

            activity?.runOnUiThread { adapter.endSelection() }
        }.start()
    }
}