package com.abbie.minipaint.view.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.abbie.minipaint.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        when (Navigation.findNavController(this,
            R.id.nav_host_fragment
        ).currentDestination?.id) {
            R.id.paintFragment -> finish()
            else -> super.onBackPressed()
        }
    }
}
