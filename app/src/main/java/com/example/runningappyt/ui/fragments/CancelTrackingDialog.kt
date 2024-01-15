package com.example.runningappyt.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.R
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog : DialogFragment() {


    private var yesListener: (() -> Unit) ? = null

    fun setYesListener(listener : () -> Unit){
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialog_AppCompat_Light)
            .setTitle("Cancel the run?")
            .setMessage("Are u sure to cancel the current run and delete all the data")
            .setIcon(com.example.runningappyt.R.drawable.ic_delete)
            .setPositiveButton("yes"){ _, _ ->
                yesListener?.let {yes  ->
                    yes()
                }
            }
            .setNegativeButton("No"){ dialogInterface , _ ->
                dialogInterface.cancel()
            }
            .create()
    }


}