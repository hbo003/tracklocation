package com.smartestmedia.tracklocation.ui.utils

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.maps.route.extensions.drawRouteOnMap
import com.maps.route.extensions.moveCameraOnMap
import com.smartestmedia.tracklocation.R
import com.smartestmedia.tracklocation.databinding.ItemPlaceBinding
import com.smartestmedia.tracklocation.ui.model.Place
import kotlinx.android.synthetic.main.item_place.view.*


object AppUtils {

    val getNavOptions: NavOptions
        get() = navOptions {
            anim {
                enter = R.anim.enter
                exit = R.anim.exit
            }
        }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun shareLocation(latitude: String, longitude: String, activity: FragmentActivity) {
        val uri = "https://www.google.com/maps/?q=$latitude,$longitude"
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain";
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, uri);
        activity.startActivity(Intent.createChooser(sharingIntent, "Share in..."))
    }

    fun GoogleMap?.setMarkersFromPlace(
        fragment: Fragment,
        place: Place,
        source: LatLng
    ) {

        var timeDistance = ""
        if (this != null) {
            this.clear()
            val builder = LatLngBounds.Builder()
            builder.include(place.latLng)
            var marker = this.addMarker(MarkerOptions().position(place.latLng).title(place.name))
            marker.tag = place
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pngwing))
            marker.isDraggable = false

            this.setOnInfoWindowClickListener {
                this@setMarkersFromPlace?.run {
                    moveCameraOnMap(latLng = source)
                    //Called the drawRouteOnMap extension to draw the polyline/route on google maps
                    drawRouteOnMap(
                        fragment.requireContext().resources.getString(R.string.google_maps_key),
                        source = source,
                        destination = place.latLng,
                        context = fragment.requireContext()
                        //Travel mode, by default it is DRIVING
                    ) { estimates ->
                        timeDistance = estimates?.duration?.text.toString()
                    }
                }

            }
            this.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
                override fun getInfoContents(marker: Marker): View {
                    var binding =
                        ItemPlaceBinding.inflate(LayoutInflater.from(fragment.requireContext()))
                    binding.apply {
                        this.place = marker.tag as Place?
                        if (timeDistance.isNotEmpty()) {
                            binding.openhourItemSalonResultFound.text = timeDistance
                        }

                        executePendingBindings()

                    }

                    return binding.root

                }

                override fun getInfoWindow(marker: Marker): View? {

                    return null
                }
            })
            val bounds = builder.build()
            this.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20))
        }


    }


}
