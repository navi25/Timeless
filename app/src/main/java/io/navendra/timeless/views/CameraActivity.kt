package io.navendra.timeless.views

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.navendra.timeless.R
import io.navendra.timeless.views.fragments.CameraFragment

class CameraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, CameraFragment.newInstance())
                    .commit()
        }
    }
}
