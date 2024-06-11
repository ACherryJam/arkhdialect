package cherryjam.narfu.arkhdialect.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment


class AlertDialogHelper(var fragment: Fragment?) {
    var alertDialog: AlertDialog? = null
    var callBack: AlertDialogListener
    var current_activity = fragment!!.requireActivity()

    init {
        callBack = fragment as AlertDialogListener
    }

    /**
     * Displays the AlertDialog with 3 Action buttons
     *
     * you can set cancelable property
     *
     * @param title
     * @param message
     * @param positive
     * @param negative
     * @param neutral
     * @param from
     * @param isCancelable
     */
    /**
     * Displays the AlertDialog with 3 Action buttons
     *
     * cancelable property is false (Default)
     *
     * @param title
     * @param message
     * @param positive
     * @param negative
     * @param neutral
     * @param from
     */
    @JvmOverloads
    fun showAlertDialog(
        title: String?,
        message: String?,
        positive: String?,
        negative: String?,
        neutral: String?,
        from: Int,
        isCancelable: Boolean = false
    ) {
        val alertDialogBuilder = AlertDialog.Builder(current_activity)

        if (!TextUtils.isEmpty(title)) alertDialogBuilder.setTitle(title)
        if (!TextUtils.isEmpty(message)) alertDialogBuilder.setMessage(message)

        if (!TextUtils.isEmpty(positive)) {
            alertDialogBuilder.setPositiveButton(
                positive
            ) { arg0, arg1 ->
                callBack.onPositiveClick(from)
                alertDialog!!.dismiss()
            }
        }
        if (!TextUtils.isEmpty(neutral)) {
            alertDialogBuilder.setNeutralButton(
                neutral
            ) { arg0, arg1 ->
                callBack.onNeutralClick(from)
                alertDialog!!.dismiss()
            }
        }
        if (!TextUtils.isEmpty(negative)) {
            alertDialogBuilder.setNegativeButton(
                negative
            ) { arg0, arg1 ->
                callBack.onNegativeClick(from)
                alertDialog!!.dismiss()
            }
        } else {
            try {
                // Возможно надо переделать (см. код в DeepSeek)
                object : Thread() {
                    override fun run() {
                        Looper.prepare()
                        alertDialog = alertDialogBuilder.create()
                        alertDialog?.show()

                        val negative_button = alertDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)
                        negative_button?.visibility = View.GONE

                        Looper.loop()
                    }
                }.start()
            } catch (e: Exception) {
                // TODO: handle exception
                e.printStackTrace()
            }
        }

        alertDialogBuilder.setCancelable(isCancelable)


        alertDialog = alertDialogBuilder.create()
        alertDialog?.show()
    }


    /**
     * Displays the AlertDialog with positive action button only
     *
     * you can set cancelable property
     *
     * @param title
     * @param message
     * @param positive
     * @param from
     * @param isCancelable
     */
    fun showAlertDialog(
        title: String?,
        message: String?,
        positive: String?,
        from: Int,
        isCancelable: Boolean
    ) {
        showAlertDialog(title, message, positive, "", "", from, isCancelable)
    }

    /**
     * Displays the AlertDialog with positive action button only
     *
     * cancelable property is false (Default)
     *
     * @param title
     * @param message
     * @param positive
     * @param from
     */
    fun showAlertDialog(title: String?, message: String?, positive: String?, from: Int) {
        showAlertDialog(title, message, positive, "", "", from, false)
    }


    /**
     *
     * Displays the AlertDialog with positive & negative buttons
     *
     * you can set cancelable property
     *
     * @param title
     * @param message
     * @param positive
     * @param negative
     * @param from
     * @param isCancelable
     */
    fun showAlertDialog(
        title: String?,
        message: String?,
        positive: String?,
        negative: String?,
        from: Int,
        isCancelable: Boolean
    ) {
        showAlertDialog(title, message, positive, negative, "", from, isCancelable)
    }

    /**
     *
     * Displays the AlertDialog with positive & negative buttons
     *
     * cancelable property is false (Default)
     *
     * @param title
     * @param message
     * @param positive
     * @param negative
     * @param from
     */
    fun showAlertDialog(
        title: String?,
        message: String?,
        positive: String?,
        negative: String?,
        from: Int
    ) {
        showAlertDialog(title, message, positive, negative, "", from, false)
    }

    interface AlertDialogListener {
        fun onPositiveClick(from: Int)
        fun onNegativeClick(from: Int)
        fun onNeutralClick(from: Int)
    }
}
