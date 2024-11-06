package com.scanner.library

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment

@SuppressLint("ValidFragment")
class ProgressDialogFragment(var message: String?) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ProgressDialog(getActivity())
        dialog.setIndeterminate(true)
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        // Disable the back button
        val keyListener: DialogInterface.OnKeyListener = object : DialogInterface.OnKeyListener {
            override fun onKey(
                dialog: DialogInterface?, keyCode: Int,
                event: KeyEvent?
            ): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true
                }
                return false
            }
        }
        dialog.setOnKeyListener(keyListener)
        return dialog
    }
}
