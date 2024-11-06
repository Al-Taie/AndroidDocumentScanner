package com.scanner.library

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

@SuppressLint("ValidFragment")
class SingleButtonDialogFragment(
    positiveButtonTitle: Int,
    message: String?, title: String?, isCancelable: Boolean
) : DialogFragment() {
    protected var positiveButtonTitle: Int
    protected var message: String?
    protected var title: String?
    protected var isCancelable: Boolean

    init {
        this.positiveButtonTitle = positiveButtonTitle
        this.message = message
        this.title = title
        this.isCancelable = isCancelable
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(getActivity())
            .setTitle(title)
            .setCancelable(isCancelable)
            .setMessage(message)
            .setPositiveButton(positiveButtonTitle,
                object : DialogInterface.OnClickListener {
                    override fun onClick(
                        dialog: DialogInterface?,
                        which: Int
                    ) {
                    }
                })

        return builder.create()
    }
}
