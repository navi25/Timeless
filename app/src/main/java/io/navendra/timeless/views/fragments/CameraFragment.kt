package io.navendra.timeless.views.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.Image
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import io.navendra.timeless.R
import io.navendra.timeless.services.CameraService
import java.util.*


class CameraFragment : Fragment(),
        View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback{


    //region Static variables & Initialisation

    /**
     * Conversion from screen rotation to JPEG orientation.
     */
    private val ORIENTATIONS = SparseIntArray()
    private val REQUEST_CAMERA_PERMISSION = 1
    private val FRAGMENT_DIALOG = "fragment_dialog"

    init {
        ORIENTATIONS.append(Surface.ROTATION_0, 90)
        ORIENTATIONS.append(Surface.ROTATION_90, 0)
        ORIENTATIONS.append(Surface.ROTATION_180, 270)
        ORIENTATIONS.append(Surface.ROTATION_270, 180)
    }

    //endregion


    /**
     * Various Camera States
     */
    enum class CAMERA_STATE{
        STATE_PREVIEW,                  // Showing Camera Preview (This will be our desired default view)
        STATE_WAITING_LOCK,             // Waiting for focus to be locked
        STATE_WAITING_PRECAPTURE,       // Waiting for the exposure to be pre captured state
        STATE_WAITING_NON_PRECAPTURE,   // Waiting for the exposure to be something other than precapture state
        STATE_PICTURE_TAKEN             // Picture was taken
    }

    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080

    private val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener{
        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }


    private fun openCamera(width:Int, height:Int){

        val cameraPermission = context?.checkSelfPermission(Manifest.permission.CAMERA)

        if( cameraPermission != PackageManager.PERMISSION_GRANTED){
            requestCameraPermission()
            return
        }



//        setUpCameraOutputs(width, height)
//        configureTransform(width, height)

    }


    //region CAMERA SETUP UTILS

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            DialogManager.CameraConfirmation()
                    .setCameraPermissionInt(REQUEST_CAMERA_PERMISSION)
                    .build()
                    .show(childFragmentManager,FRAGMENT_DIALOG)

        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }


    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width:Int, height: Int){
        val manager = activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for(cameraId in manager.cameraIdList){
            val characteristics = manager.getCameraCharacteristics(cameraId)

            //get characteristics

            val cameraFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
            val streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            streamConfigurationMap?:continue

//            val largest = Collections.max(
//                    Arrays.asList(streamConfigurationMap.getOutputSizes(ImageFormat.JPEG)),
////                    CompareSize
//            )



        }

    }

    //endregion


    override fun onClick(v: View?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}