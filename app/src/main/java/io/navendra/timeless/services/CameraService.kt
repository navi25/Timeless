//package io.navendra.timeless.services
//
//import android.Manifest
//import android.app.AlertDialog
//import android.app.Dialog
//import android.content.Context
//import android.content.Context.CAMERA_SERVICE
//import android.content.DialogInterface
//import android.content.pm.PackageManager
//import android.content.res.Configuration
//import android.graphics.*
//import android.hardware.camera2.*
//import android.hardware.camera2.params.StreamConfigurationMap
//import android.media.Image
//import android.media.ImageReader
//import android.os.Bundle
//import android.os.Handler
//import android.os.HandlerThread
//import android.support.v4.app.ActivityCompat.requestPermissions
//import android.support.v4.app.ActivityCompat.shouldShowRequestPermissionRationale
//import android.support.v4.app.DialogFragment
//import android.support.v4.content.ContextCompat
//import android.util.Log
//import android.util.Size
//import android.util.SparseIntArray
//import android.view.*
//import android.widget.Toast
//import io.navendra.timeless.R
//import io.navendra.timeless.views.custom.FullScreenTextureView
//import io.navendra.timeless.views.DialogManager
//import java.io.File
//import java.io.FileOutputStream
//import java.io.IOException
//import java.util.*
//import java.util.concurrent.Semaphore
//import java.util.concurrent.TimeUnit
//
//
//class CameraService(private val context: Context, private val mTextureView:FullScreenTextureView) {
//
//    val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
//
//    /**
//     * Conversion from screen rotation to JPEG orientation.
//     */
//    private val ORIENTATIONS = SparseIntArray()
//    private val REQUEST_CAMERA_PERMISSION = 1
//    private val FRAGMENT_DIALOG = "fragment_dialog"
//
//    init {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90)
//        ORIENTATIONS.append(Surface.ROTATION_90, 0)
//        ORIENTATIONS.append(Surface.ROTATION_180, 270)
//        ORIENTATIONS.append(Surface.ROTATION_270, 180)
//    }
//
//
//    /**
//     * Various Camera States
//     */
//    enum class CAMERA_STATE{
//        STATE_PREVIEW,                  // Showing Camera Preview (This will be our desired default view)
//        STATE_WAITING_LOCK,             // Waiting for focus to be locked
//        STATE_WAITING_PRECAPTURE,       // Waiting for the exposure to be pre captured state
//        STATE_WAITING_NON_PRECAPTURE,   // Waiting for the exposure to be something other than precapture state
//        STATE_PICTURE_TAKEN             // Picture was taken
//    }
//
//    private val MAX_PREVIEW_WIDTH = 1920
//    private val MAX_PREVIEW_HEIGHT = 1080
//
//    val mSurfaceTextureListener = object : TextureView.SurfaceTextureListener{
//        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//        }
//
//    }
//
//
//
//    /**
//     * ID of the current [CameraDevice].
//     */
//    private var mCameraId: String? = null
//
//
//    /**
//     * A [CameraCaptureSession] for camera preview.
//     */
//    private var mCaptureSession: CameraCaptureSession? = null
//
//    /**
//     * A reference to the opened [CameraDevice].
//     */
//    private var mCameraDevice: CameraDevice? = null
//
//    /**
//     * The [android.util.Size] of camera preview.
//     */
//    private var mPreviewSize: Size? = null
//
//
//
//
//    /**
//     * An additional thread for running tasks that shouldn't block the UI.
//     */
//    private var mBackgroundThread: HandlerThread? = null
//
//    /**
//     * A [Handler] for running tasks in the background.
//     */
//    private var mBackgroundHandler: Handler? = null
//
//    /**
//     * An [ImageReader] that handles still image capture.
//     */
//    private var mImageReader: ImageReader? = null
//
//    /**
//     * This is the output file for our picture.
//     */
//    private var mFile: File? = null
//
//    /**
//     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
//     * still image is ready to be saved.
//     */
//    private val mOnImageAvailableListener = ImageReader.OnImageAvailableListener { reader -> mBackgroundHandler!!.post(ImageSaver(reader.acquireNextImage(), mFile!!)) }
//
//    /**
//     * [CaptureRequest.Builder] for the camera preview
//     */
//    private var mPreviewRequestBuilder: CaptureRequest.Builder? = null
//
//    /**
//     * [CaptureRequest] generated by [.mPreviewRequestBuilder]
//     */
//    private var mPreviewRequest: CaptureRequest? = null
//
//    /**
//     * The current state of camera state for taking pictures.
//     *
//     * @see .mCaptureCallback
//     */
//    private var mState = CAMERA_STATE.STATE_PREVIEW
//
//    /**
//     * A [Semaphore] to prevent the app from exiting before closing the camera.
//     */
//    private val mCameraOpenCloseLock = Semaphore(1)
//
//    /**
//     * Whether the current camera device supports Flash or not.
//     */
//    private var mFlashSupported: Boolean = false
//
//    /**
//     * Orientation of the camera sensor
//     */
//    private var mSensorOrientation: Int = 0
//
//    /**
//     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
//     */
//    private val mCaptureCallback = object : CameraCaptureSession.CaptureCallback() {
//
//        private fun process(result: CaptureResult) {
//            when (mState) {
//                CAMERA_STATE.STATE_PREVIEW -> {
//                    // We have nothing to do when the camera preview is working normally.
//                }
//                CAMERA_STATE.STATE_WAITING_LOCK -> {
//                    val afState = result.get(CaptureResult.CONTROL_AF_STATE)
//                    if (afState == null) {
//                        captureStillPicture()
//                    } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
//                        // CONTROL_AE_STATE can be null on some devices
//                        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
//                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
//                            mState = CAMERA_STATE.STATE_PICTURE_TAKEN
//                            captureStillPicture()
//                        } else {
//                            runPrecaptureSequence()
//                        }
//                    }
//                }
//                CAMERA_STATE.STATE_WAITING_PRECAPTURE -> {
//                    // CONTROL_AE_STATE can be null on some devices
//                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
//                    if (aeState == null ||
//                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
//                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
//                        mState = CAMERA_STATE.STATE_WAITING_NON_PRECAPTURE
//                    }
//                }
//                CAMERA_STATE.STATE_WAITING_NON_PRECAPTURE -> {
//                    // CONTROL_AE_STATE can be null on some devices
//                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
//                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
//                        mState = CAMERA_STATE.STATE_PICTURE_TAKEN
//                        captureStillPicture()
//                    }
//                }
//            }
//        }
//
//        override fun onCaptureProgressed(session: CameraCaptureSession,
//                                         request: CaptureRequest,
//                                         partialResult: CaptureResult) {
//            process(partialResult)
//        }
//
//        override fun onCaptureCompleted(session: CameraCaptureSession,
//                                        request: CaptureRequest,
//                                        result: TotalCaptureResult) {
//            process(result)
//        }
//
//    }
//
//
//    /**
//     * Given `choices` of `Size`s supported by a camera, choose the smallest one that
//     * is at least as large as the respective texture view size, and that is at most as large as the
//     * respective max size, and whose aspect ratio matches with the specified value. If such size
//     * doesn't exist, choose the largest one that is at most as large as the respective max size,
//     * and whose aspect ratio matches with the specified value.
//     *
//     * @param choices           The list of sizes that the camera supports for the intended output
//     * class
//     * @param textureViewWidth  The width of the texture view relative to sensor coordinate
//     * @param textureViewHeight The height of the texture view relative to sensor coordinate
//     * @param maxWidth          The maximum width that can be chosen
//     * @param maxHeight         The maximum height that can be chosen
//     * @param aspectRatio       The aspect ratio
//     * @return The optimal `Size`, or an arbitrary one if none were big enough
//     */
//    private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int,
//                                  textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {
//
//        // Collect the supported resolutions that are at least as big as the preview Surface
//        val bigEnough = ArrayList<Size>()
//        // Collect the supported resolutions that are smaller than the preview Surface
//        val notBigEnough = ArrayList<Size>()
//        val w = aspectRatio.width
//        val h = aspectRatio.height
//        for (option in choices) {
//            if (option.width <= maxWidth && option.height <= maxHeight &&
//                    option.height == option.width * h / w) {
//                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
//                    bigEnough.add(option)
//                } else {
//                    notBigEnough.add(option)
//                }
//            }
//        }
//
//        // Pick the smallest of those big enough. If there is no one big enough, pick the
//        // largest of those not big enough.
//        if (bigEnough.size > 0) {
//            return Collections.min(bigEnough, CompareSizesByArea())
//        } else if (notBigEnough.size > 0) {
//            return Collections.max(notBigEnough, CompareSizesByArea())
//        } else {
//            Log.e("CameraService", "Couldn't find any suitable preview size")
//            return choices[0]
//        }
//    }
//
//
//
//
////    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
////                                            grantResults: IntArray) {
////        if (requestCode == REQUEST_CAMERA_PERMISSION) {
////            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
////                ErrorDialog.newInstance(getString(R.string.request_permission))
////                        .show(getChildFragmentManager(), FRAGMENT_DIALOG)
////            }
////        } else {
////            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////        }
////    }
//
//    /**
//     * Sets up member variables related to camera.
//     *
//     * @param width  The width of available size for camera preview
//     * @param height The height of available size for camera preview
//     */
//    private fun setUpCameraOutputs(width: Int, height: Int) {
//        try {
//            for (cameraId in manager.cameraIdList) {
//                val characteristics = manager.getCameraCharacteristics(cameraId)
//
//                // We don't use a front facing camera in this sample.
//                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
//                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
//                    continue
//                }
//
//                val map = characteristics.get(
//                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue
//
//                // For still image captures, we use the largest available size.
//                val largest = Collections.max(
//                        Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
//                        CompareSizesByArea())
//                mImageReader = ImageReader.newInstance(largest.width, largest.height,
//                        ImageFormat.JPEG, /*maxImages*/2)
//                mImageReader!!.setOnImageAvailableListener(
//                        mOnImageAvailableListener, mBackgroundHandler)
//
//
//                // Find out if we need to swap dimension to get the preview size relative to sensor
//                // coordinate.
//                val displayRotation = context!!.getWindowManager().getDefaultDisplay().getRotation()
//
//                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
//                var swappedDimensions = false
//                when (displayRotation) {
//                    Surface.ROTATION_0, Surface.ROTATION_180 -> if (mSensorOrientation == 90 || mSensorOrientation == 270) {
//                        swappedDimensions = true
//                    }
//                    Surface.ROTATION_90, Surface.ROTATION_270 -> if (mSensorOrientation == 0 || mSensorOrientation == 180) {
//                        swappedDimensions = true
//                    }
//                    else -> Log.e("CameraService", "Display rotation is invalid: $displayRotation")
//                }
//
//                val displaySize = Point()
//                activity!!.getWindowManager().getDefaultDisplay().getSize(displaySize)
//                var rotatedPreviewWidth = width
//                var rotatedPreviewHeight = height
//                var maxPreviewWidth = displaySize.x
//                var maxPreviewHeight = displaySize.y
//
//                if (swappedDimensions) {
//                    rotatedPreviewWidth = height
//                    rotatedPreviewHeight = width
//                    maxPreviewWidth = displaySize.y
//                    maxPreviewHeight = displaySize.x
//                }
//
//                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
//                    maxPreviewWidth = MAX_PREVIEW_WIDTH
//                }
//
//                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
//                    maxPreviewHeight = MAX_PREVIEW_HEIGHT
//                }
//
//                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
//                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
//                // garbage capture data.
//                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture::class.java),
//                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
//                        maxPreviewHeight, largest)
//
//                // We fit the aspect ratio of TextureView to the size of preview we picked.
//                val orientation = getResources().getConfiguration().orientation
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                    mTextureView!!.setAspectRatio(
//                            mPreviewSize!!.width, mPreviewSize!!.height)
//                } else {
//                    mTextureView!!.setAspectRatio(
//                            mPreviewSize!!.height, mPreviewSize!!.width)
//                }
//
//                // Check if the flash is supported.
//                val available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
//                mFlashSupported = available ?: false
//
//                mCameraId = cameraId
//                return
//            }
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: NullPointerException) {
//            // Currently an NPE is thrown when the Camera2API is used but not supported on the
//            // device this code runs.
//            ErrorDialog.newInstance(getString(R.string.camera_error))
//                    .show(getChildFragmentManager(), FRAGMENT_DIALOG)
//        }
//
//    }
//
//    /**
//     * Opens the camera specified by [Camera2BasicFragment.mCameraId].
//     */
//    fun openCamera(width: Int, height: Int) {
//        val cameraPermission = context.checkSelfPermission(Manifest.permission.CAMERA)
//        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
//            requestCameraPermission()
//            return
//        }
//        setUpCameraOutputs(width, height)
//        configureTransform(width, height)
//        try {
//            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
//                throw RuntimeException("Time out waiting to lock camera opening.")
//            }
//            manager.openCamera(mCameraId!!, mStateCallback, mBackgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        } catch (e: InterruptedException) {
//            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
//        }
//
//    }
//
//    /**
//     * Closes the current [CameraDevice].
//     */
//    fun closeCamera() {
//        try {
//            mCameraOpenCloseLock.acquire()
//            if (null != mCaptureSession) {
//                mCaptureSession!!.close()
//                mCaptureSession = null
//            }
//            if (null != mCameraDevice) {
//                mCameraDevice!!.close()
//                mCameraDevice = null
//            }
//            if (null != mImageReader) {
//                mImageReader!!.close()
//                mImageReader = null
//            }
//        } catch (e: InterruptedException) {
//            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
//        } finally {
//            mCameraOpenCloseLock.release()
//        }
//    }
//
//    /**
//     * Starts a background thread and its [Handler].
//     */
//    fun startBackgroundThread() {
//        mBackgroundThread = HandlerThread("CameraBackground")
//        mBackgroundThread!!.start()
//        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
//    }
//
//    /**
//     * Stops the background thread and its [Handler].
//     */
//    fun stopBackgroundThread() {
//        mBackgroundThread!!.quitSafely()
//        try {
//            mBackgroundThread!!.join()
//            mBackgroundThread = null
//            mBackgroundHandler = null
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    /**
//     * Creates a new [CameraCaptureSession] for camera preview.
//     */
//    private fun createCameraPreviewSession() {
//        try {
//            val texture = mTextureView!!.getSurfaceTexture()!!
//
//            // We configure the size of default buffer to be the size of camera preview we want.
//            texture!!.setDefaultBufferSize(mPreviewSize!!.width, mPreviewSize!!.height)
//
//            // This is the output Surface we need to start preview.
//            val surface = Surface(texture)
//
//            // We set up a CaptureRequest.Builder with the output Surface.
//            mPreviewRequestBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//            mPreviewRequestBuilder!!.addTarget(surface)
//
//            // Here, we create a CameraCaptureSession for camera preview.
//            mCameraDevice!!.createCaptureSession(Arrays.asList(surface, mImageReader!!.surface),
//                    object : CameraCaptureSession.StateCallback() {
//
//                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
//                            // The camera is already closed
//                            if (null == mCameraDevice) {
//                                return
//                            }
//
//                            // When the session is ready, we start displaying the preview.
//                            mCaptureSession = cameraCaptureSession
//                            try {
//                                // Auto focus should be continuous for camera preview.
//                                mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_MODE,
//                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
//                                // Flash is automatically enabled when necessary.
//                                setAutoFlash(mPreviewRequestBuilder)
//
//                                // Finally, we start displaying the camera preview.
//                                mPreviewRequest = mPreviewRequestBuilder!!.build()
//                                mCaptureSession!!.setRepeatingRequest(mPreviewRequest!!,
//                                        mCaptureCallback, mBackgroundHandler)
//                            } catch (e: CameraAccessException) {
//                                e.printStackTrace()
//                            }
//
//                        }
//
//                        override fun onConfigureFailed(
//                                cameraCaptureSession: CameraCaptureSession) {
//                            showToast("Failed")
//                        }
//                    }, null
//            )
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    /**
//     * Configures the necessary [android.graphics.Matrix] transformation to `mTextureView`.
//     * This method should be called after the camera preview size is determined in
//     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
//     *
//     * @param viewWidth  The width of `mTextureView`
//     * @param viewHeight The height of `mTextureView`
//     */
//    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
//        val activity = getActivity()
//        if (null == mTextureView || null == mPreviewSize || null == activity) {
//            return
//        }
//        val rotation = activity!!.getWindowManager().getDefaultDisplay().getRotation()
//        val matrix = Matrix()
//        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
//        val bufferRect = RectF(0f, 0f, mPreviewSize!!.height.toFloat(), mPreviewSize!!.width.toFloat())
//        val centerX = viewRect.centerX()
//        val centerY = viewRect.centerY()
//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
//            val scale = Math.max(
//                    viewHeight.toFloat() / mPreviewSize!!.height,
//                    viewWidth.toFloat() / mPreviewSize!!.width)
//            matrix.postScale(scale, scale, centerX, centerY)
//            matrix.postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
//        } else if (Surface.ROTATION_180 == rotation) {
//            matrix.postRotate(180f, centerX, centerY)
//        }
//        mTextureView!!.setTransform(matrix)
//    }
//
//    /**
//     * Initiate a still image capture.
//     */
//    private fun takePicture() {
//        lockFocus()
//    }
//
//    /**
//     * Lock the focus as the first step for a still image capture.
//     */
//    private fun lockFocus() {
//        try {
//            // This is how to tell the camera to lock focus.
//            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                    CameraMetadata.CONTROL_AF_TRIGGER_START)
//            // Tell #mCaptureCallback to wait for the lock.
//            mState = STATE_WAITING_LOCK
//            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
//                    mBackgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    /**
//     * Run the precapture sequence for capturing a still image. This method should be called when
//     * we get a response in [.mCaptureCallback] from [.lockFocus].
//     */
//    private fun runPrecaptureSequence() {
//        try {
//            // This is how to tell the camera to trigger.
//            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
//                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START)
//            // Tell #mCaptureCallback to wait for the precapture sequence to be set.
//            mState = STATE_WAITING_PRECAPTURE
//            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
//                    mBackgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    /**
//     * Capture a still picture. This method should be called when we get a response in
//     * [.mCaptureCallback] from both [.lockFocus].
//     */
//    private fun captureStillPicture() {
//        try {
//            val activity = getActivity()
//            if (null == activity || null == mCameraDevice) {
//                return
//            }
//            // This is the CaptureRequest.Builder that we use to take a picture.
//            val captureBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
//            captureBuilder.addTarget(mImageReader!!.surface)
//
//            // Use the same AE and AF modes as the preview.
//            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
//                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
//            setAutoFlash(captureBuilder)
//
//            // Orientation
//            val rotation = activity!!.getWindowManager().getDefaultDisplay().getRotation()
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation))
//
//            val CaptureCallback = object : CameraCaptureSession.CaptureCallback() {
//
//                override fun onCaptureCompleted(session: CameraCaptureSession,
//                                                request: CaptureRequest,
//                                                result: TotalCaptureResult) {
//                    showToast("Saved: " + mFile!!)
//                    Log.d(TAG, mFile!!.toString())
//                    unlockFocus()
//                }
//            }
//
//            mCaptureSession!!.stopRepeating()
//            mCaptureSession!!.abortCaptures()
//            mCaptureSession!!.capture(captureBuilder.build(), CaptureCallback, null)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    /**
//     * Retrieves the JPEG orientation from the specified screen rotation.
//     *
//     * @param rotation The screen rotation.
//     * @return The JPEG orientation (one of 0, 90, 270, and 360)
//     */
//    private fun getOrientation(rotation: Int): Int {
//        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
//        // We have to take that into account and rotate JPEG properly.
//        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
//        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
//        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360
//    }
//
//    /**
//     * Unlock the focus. This method should be called when still image capture sequence is
//     * finished.
//     */
//    private fun unlockFocus() {
//        try {
//            // Reset the auto-focus trigger
//            mPreviewRequestBuilder!!.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                    CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
//            setAutoFlash(mPreviewRequestBuilder)
//            mCaptureSession!!.capture(mPreviewRequestBuilder!!.build(), mCaptureCallback,
//                    mBackgroundHandler)
//            // After this, the camera will go back to the normal state of preview.
//            mState = STATE_PREVIEW
//            mCaptureSession!!.setRepeatingRequest(mPreviewRequest!!, mCaptureCallback,
//                    mBackgroundHandler)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//
//    }
//
//    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder?) {
//        if (mFlashSupported) {
//            requestBuilder!!.set(CaptureRequest.CONTROL_AE_MODE,
//                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
//        }
//    }
//
//
//
//    /**
//     * Compares two `Size`s based on their areas.
//     */
//    internal class CompareSizesByArea : Comparator<Size> {
//
//        override fun compare(lhs: Size, rhs: Size): Int {
//            // We cast here to ensure the multiplications won't overflow
//            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
//        }
//
//    }
//
//
//
//
//}