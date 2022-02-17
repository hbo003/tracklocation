package com.smartestmedia.tracklocation.ui.fragment

import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.smartestmedia.tracklocation.R
import com.smartestmedia.tracklocation.ui.utils.AppUtils.getNavOptions


class SplashFragment : Fragment(R.layout.fragment_splash) {
   private val SPLASH_TIME_OUT: Long = 1000

    override fun onResume() {
        super.onResume()
        goToHome()
    }

    private fun goToHome() {
        Handler().postDelayed({
            findNavController().popBackStack()
            findNavController().navigate(R.id.nearbyFragment, null, getNavOptions)

        }, SPLASH_TIME_OUT)
    }
}