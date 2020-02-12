package com.abbie.minipaint.view.home

import android.os.Bundle
import android.view.View
import androidx.navigation.Navigation
import com.abbie.minipaint.R
import com.abbie.minipaint.view.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : BaseFragment() {
    override fun getLayoutId() = R.layout.fragment_home

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_paint.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_homeFragment_to_paintFragment)
        }
    }
}