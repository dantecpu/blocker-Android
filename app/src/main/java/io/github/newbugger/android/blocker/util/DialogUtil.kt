package io.github.newbugger.android.blocker.util

import android.content.Context
import android.content.DialogInterface
import android.os.RemoteException
import androidx.appcompat.app.AlertDialog
import io.github.newbugger.android.blocker.R

class DialogUtil {

    fun showWarningDialogWithMessage(context: Context?, e: Throwable) {
        context?.apply {
            val errorInfo = StringUtil.getStackTrace(e)
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.oops))
                    .setMessage(getString(R.string.no_root_error_message, errorInfo))
                    .setPositiveButton(R.string.close) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .show()
        }
    }

    fun showWarningDialogWithMessageR(context: Context?, e: RemoteException) {
        context?.apply {
            val errorInfo = StringUtil.getStackTrace(e)
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.oops))
                    .setMessage(getString(R.string.no_root_error_message, errorInfo))
                    .setPositiveButton(R.string.close) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
                    .show()
        }
    }

}
