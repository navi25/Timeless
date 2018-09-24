package io.navendra.timeless.views

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import io.navendra.timeless.App
import io.navendra.timeless.R
import io.navendra.timeless.utils.TimelessConstant
import io.navendra.timeless.viewmodels.FiltersViewModel
import io.navendra.timeless.views.adapters.FiltersAdapter
import kotlinx.android.synthetic.main.activity_edit_photo.*

class EditPhotoActivity : AppCompatActivity() {

    lateinit var  filtersViewModel : FiltersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_photo)
        val imagePath = intent.extras.get(TimelessConstant.IMAGE_URI) as String
        filtersViewModel = (application as App).getFilterViewModel()
        setupImage(imagePath)
        initRecycler()
    }

    fun setupImage(imagePath : String){
        Glide.with(this).load(imagePath).into(imageView)
    }

    fun initRecycler(){
        val linearLayoutManager = LinearLayoutManager(this,LinearLayout.HORIZONTAL,false)
        val filterAdapter = FiltersAdapter(filtersViewModel.filters)

        recyclerView.apply {
            layoutManager = linearLayoutManager
            adapter = filterAdapter
        }

    }
}
