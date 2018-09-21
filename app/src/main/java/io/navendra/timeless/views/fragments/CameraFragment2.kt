//package io.navendra.timeless.views.fragments
//
//import android.Manifest
//import android.app.AlertDialog
//import android.hardware.camera2.CameraDevice
//import android.os.Bundle
//import android.support.v4.app.ActivityCompat
//import android.support.v4.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import io.navendra.timeless.R
//import io.navendra.timeless.services.CameraService
//import io.navendra.timeless.views.custom.FullScreenTextureView
//import kotlinx.android.synthetic.main.fragment_camera.*
//import java.io.File
//
//
//class CameraFragment : Fragment(),
//        View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback{
//
//    lateinit var mFile : File
//    lateinit var mTextureView : FullScreenTextureView
//    lateinit var mCameraService: CameraService
//
//
//    fun newInstance(): CameraFragment {
//        return CameraFragment()
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_camera, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        button.setOnClickListener(this)
//        mTextureView = textureView as FullScreenTextureView
//        mCameraService = CameraService(context!!)
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        mFile = File(activity!!.getExternalFilesDir(null), "pic.jpg")
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mCameraService.startBackgroundThread()
//
//        /**
//        * When the screen is turned off and turned back on, the SurfaceTexture is already
//        * available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
//        * a camera and start preview from here (otherwise, we wait until the surface is ready in
//        * the SurfaceTextureListener.
//        **/
//        if (mTextureView.isAvailable) {
//            mCameraService.openCamera(mTextureView.width, mTextureView.height)
//        } else {
//            mTextureView.surfaceTextureListener = mCameraService.mSurfaceTextureListener
//        }
//    }
//
//    override fun onPause() {
//        mCameraService.closeCamera()
//        mCameraService.stopBackgroundThread()
//        super.onPause()
//    }
//
//    //endregion
//
//
//    /**
//     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
//     */
//    private val mStateCallback = object : CameraDevice.StateCallback() {
//
//        override fun onOpened(cameraDevice: CameraDevice) {
//            // This method is called when the camera is opened.  We start camera preview here.
//            mCameraService.mCameraOpenCloseLock.release()
//            mCameraService.mCameraDevice = cameraDevice
//            mCameraService.createCameraPreviewSession()
//        }
//
//        override fun onDisconnected(cameraDevice: CameraDevice) {
//            mCameraOpenCloseLock.release()
//            cameraDevice.close()
//            mCameraDevice = null
//        }
//
//        override fun onError(cameraDevice: CameraDevice, error: Int) {
//            mCameraOpenCloseLock.release()
//            cameraDevice.close()
//            mCameraDevice = null
//            val activity = getActivity()
//            if (null != activity) {
//                activity!!.finish()
//            }
//        }
//
//    }
//
//    /**
//     * Shows a [Toast] on the UI thread.
//     *
//     * @param text The message to show
//     */
//    private fun showToast(text: String) {
//        if (activity != null) {
//            activity!!.runOnUiThread{
//                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//
//    private fun requestCameraPermission() {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
//            DialogManager.CameraConfirmation().show(fragmentManager, FRAGMENT_DIALOG)
//        } else {
//            ActivityCompat.requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
//        }
//    }
//
//
//    override fun onClick(view: View) {
//        when (view.id) {
////            R.id.picture -> {
////                takePicture()
////            }
////            R.id.info -> {
////                if (null != activity) {
////                    AlertDialog.Builder(activity)
////                            .setMessage(R.string.intro_message)
////                            .setPositiveButton(android.R.string.ok, null)
////                            .show()
////                }
////            }
//        }
//    }
//
//}