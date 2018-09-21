package io.navendra.timeless.services

import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.hardware.camera2.CameraManager



class CameraService(context: Context) {

    private var cameraManager : CameraManager? = context.getSystemService(CAMERA_SERVICE) as CameraManager


}