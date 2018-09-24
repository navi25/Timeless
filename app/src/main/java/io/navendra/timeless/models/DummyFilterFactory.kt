package io.navendra.timeless.models

import io.navendra.timeless.R
import java.util.*

object DummyFilterFactory{

    val titles = arrayListOf("Rumi","Fanna", "Izhaar", "Dew", "First Love", "Breathless")

    fun getRandomString() : String{
        return UUID.randomUUID().toString()
    }

    fun getFilters() :List<TimelessFilter>{
        val filters = mutableListOf<TimelessFilter>()
        for(title in titles){
            filters.add(TimelessFilter(getRandomString(),title, R.drawable.snow))
        }
        return filters
    }

}