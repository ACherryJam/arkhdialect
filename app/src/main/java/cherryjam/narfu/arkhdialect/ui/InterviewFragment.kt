package cherryjam.narfu.arkhdialect.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import cherryjam.narfu.arkhdialect.R
import cherryjam.narfu.arkhdialect.adapter.InterviewAdapter
import cherryjam.narfu.arkhdialect.data.Interview
import cherryjam.narfu.arkhdialect.databinding.FragmentInterviewBinding
import cherryjam.narfu.arkhdialect.service.interview.FakerInterviewService
import cherryjam.narfu.arkhdialect.service.interview.InterviewService
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper
import cherryjam.narfu.arkhdialect.utils.AlertDialogHelper.AlertDialogListener
import cherryjam.narfu.arkhdialect.utils.RecyclerItemClickListener

class InterviewFragment : Fragment(), AlertDialogListener {
    private val binding: FragmentInterviewBinding by lazy {
        FragmentInterviewBinding.inflate(layoutInflater)
    }

    private lateinit var adapter: InterviewAdapter
    private val service: InterviewService = FakerInterviewService()

    // new code
    private lateinit var alertDialogHelper: AlertDialogHelper
    private lateinit var multiselect_data: MutableList<Interview>
    private lateinit var context_menu: Menu
    private var isMultiSelect: Boolean = false
    private var mActionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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

        // new code
        alertDialogHelper = AlertDialogHelper(this)
        isMultiSelect = false
        multiselect_data = arrayListOf()


        binding.interviews.addOnItemTouchListener(
            RecyclerItemClickListener(
                context,
                binding.interviews,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        // save it?
                        if (isMultiSelect)
                            multi_select(position)
                        else
                            Toast.makeText(context, "Details Page", Toast.LENGTH_SHORT).show()
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        if (!isMultiSelect) {
                            multiselect_data = arrayListOf()
                            isMultiSelect = true

                            if (mActionMode == null) {
                                mActionMode = (activity as MainActivity?)!!.startSupportActionMode(
                                    mActionModeCallback
                                )!!
                            }
                        }

                        multi_select(position)
                    }
                }
            )
        )
    }

    private fun multi_select(position: Int) {
        if (mActionMode != null) {
            if (multiselect_data.contains(adapter.data[position]))
                multiselect_data.remove(adapter.data[position])
            else
                multiselect_data.add(adapter.data[position])

            binding.searchItem.visibility = View.GONE

            if (multiselect_data.size > 0)
                mActionMode!!.title = "" + multiselect_data.size
            else
                mActionMode!!.title = ""

            refreshAdapter()
        }
    }

    private fun refreshAdapter() {
        adapter.selected_data = multiselect_data
        adapter.data = service.getData()
        adapter.notifyDataSetChanged()
    }

    private val mActionModeCallback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater = mode?.menuInflater
            inflater?.inflate(R.menu.menu_multi_select, menu)
            context_menu = menu!!
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false // Return false if nothing is done
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_delete -> {
                    alertDialogHelper!!.showAlertDialog(
                        "",
                        "Delete Contact",
                        "DELETE",
                        "CANCEL",
                        1,
                        false
                    )
                    return true
                }

                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            mActionMode = null
            isMultiSelect = false
            multiselect_data = arrayListOf()
            refreshAdapter()

            // return search View
            binding.searchItem.visibility = View.VISIBLE
        }
    }

    override fun onPositiveClick(from: Int) {
        if (from == 1) {
            if (multiselect_data.size > 0) {
                for (i in multiselect_data.indices) adapter.data.remove(multiselect_data[i])

                adapter.notifyDataSetChanged()

                if (mActionMode != null) {
                    mActionMode!!.finish()
                }
                //Toast.makeText((activity as MainActivity?)!!.applicationContext, "Delete Click", Toast.LENGTH_SHORT).show()
            }
        } else if (from == 2) {
            if (mActionMode != null) {
                mActionMode!!.finish()
            }


            adapter.notifyDataSetChanged()
        }
    }

    override fun onNegativeClick(from: Int) {
    }

    override fun onNeutralClick(from: Int) {
    }
}