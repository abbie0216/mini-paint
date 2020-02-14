package com.abbie.minipaint.view.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.navigation.Navigation
import com.abbie.minipaint.R
import com.abbie.minipaint.view.base.BaseFragment
import timber.log.Timber

class SplashFragment : BaseFragment() {

    override fun getLayoutId() = R.layout.fragment_splash

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    private val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissions()
    }

    private fun onAllPermissionsGranted() {
        Timber.d("onAllPermissionsGranted")
        Navigation.findNavController(view!!).navigate(R.id.action_splashFragment_to_paintFragment)
    }

    private fun requestPermissions() {
        val requestList = arrayListOf<String>()
        for (i in permissions.indices) {
            if (checkSelfPermission(
                    context!!,
                    permissions[i]
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestList.add(permissions[i])
            }
        }

        if (requestList.size > 0) {
            requestPermissions(requestList.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            onAllPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var isPermissionAllGranted = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    isPermissionAllGranted = false
                    break
                }
            }
            if (isPermissionAllGranted) {
                onAllPermissionsGranted()
            } else {
                requestPermissions()
            }
        }
    }
}