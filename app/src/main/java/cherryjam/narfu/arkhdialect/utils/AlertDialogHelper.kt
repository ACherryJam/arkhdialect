package cherryjam.narfu.arkhdialect.utils

import android.app.AlertDialog
import android.content.Context


class AlertDialogHelper {
    companion object {
        /**
         * Displays constructed AlertDialog
         *
         * @param title
         * @param message
         * @param positiveText
         * @param neutralText
         * @param negativeText
         * @param positiveCallback Function to call on positive button click
         * @param neutralCallback Function to call on neutral button click
         * @param negativeCallback Function to call on negative button click
         * @param isCancelable
         */
        fun showAlertDialog(
            context: Context,
            title: String? = null,
            message: String? = null,
            positiveText: String? = null,
            neutralText: String? = null,
            negativeText: String? = null,
            positiveCallback: () -> Unit = {},
            neutralCallback: () -> Unit = {},
            negativeCallback: () -> Unit = {},
            isCancelable: Boolean = false
        ) {
            val alertDialogBuilder = AlertDialog.Builder(context)

            title?.let { alertDialogBuilder.setTitle(title) }
            message?.let { alertDialogBuilder.setMessage(message) }

            positiveText?.let { alertDialogBuilder.setPositiveButton(positiveText) { _, _ -> positiveCallback() } }
            neutralText?.let { alertDialogBuilder.setNeutralButton(neutralText) { _, _ -> neutralCallback() } }
            negativeText?.let { alertDialogBuilder.setNegativeButton(negativeText) { _, _ -> negativeCallback() } }

            alertDialogBuilder.setCancelable(isCancelable)
            alertDialogBuilder.create().show()
        }
    }
}
