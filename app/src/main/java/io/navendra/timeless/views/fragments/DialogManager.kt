package io.navendra.timeless.views.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import io.navendra.timeless.R

object DialogManager{

    class CameraConfirmation() : DialogFragment(){

        private var REQUEST_CAMERA_PERMISSION : Int = 1

        fun setCameraPermissionInt(value : Int) : CameraConfirmation{
            REQUEST_CAMERA_PERMISSION = value
            return this
        }

        fun build() : CameraConfirmation{
            return this
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val parent = parentFragment
            return AlertDialog.Builder(activity)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                        parent!!.requestPermissions(arrayOf(Manifest.permission.CAMERA),
                                REQUEST_CAMERA_PERMISSION)
                    })
                    .setNegativeButton(android.R.string.cancel,
                            DialogInterface.OnClickListener { dialog, which ->
                                val activity = parent!!.activity
                                activity?.finish()
                            })
                    .create()
        }
    }
}