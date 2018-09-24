package io.navendra.timeless

import android.app.Application
import io.navendra.timeless.viewmodels.FiltersViewModel

class App : Application(){

    override fun onCreate() {
        super.onCreate()
    }

    fun getFilterViewModel() : FiltersViewModel{
        return FiltersViewModel()
    }
}