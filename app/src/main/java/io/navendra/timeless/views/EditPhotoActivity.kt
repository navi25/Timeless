package io.navendra.timeless.views

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import io.navendra.timeless.R
import io.navendra.timeless.utils.TimelessConstant
import kotlinx.android.synthetic.main.activity_edit_photo.*

class EditPhotoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)
        val imagePath = intent.extras.get(TimelessConstant.IMAGE_URI) as String
        setupImage(imagePath)
    }

    fun setupImage(imagePath : String){
        Glide.with(this).load(imagePath).into(imageView)
    }
}
