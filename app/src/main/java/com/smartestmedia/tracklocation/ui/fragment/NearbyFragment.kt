package com.smartestmedia.tracklocation.ui.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.smartestmedia.tracklocation.R
import com.smartestmedia.tracklocation.ui.model.Place
import com.smartestmedia.tracklocation.ui.model.placeStatic
import com.smartestmedia.tracklocation.ui.utils.AppUtils.setMarkersFromPlace
import com.smartestmedia.tracklocation.ui.utils.AppUtils.shareLocation
import com.smartestmedia.tracklocation.ui.utils.SearchViewAdapter
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted
import kotlinx.android.synthetic.main.fragment_nearby.*


class NearbyFragment : Fragment(R.layout.fragment_nearby), OnMapReadyCallback {

    companion object {
        // Permission Fine Location
        private const val REQUEST_CODE_FINE_LOCATION = 1
    }

    private var mInterstitialAd: InterstitialAd? = null
    private var TAG = "MainActivity"

    // Location & Map
    private var mMap: GoogleMap? = null
    private var mapsSupported = true
    var clicked = false
    var initLocation: LatLng? = null
    private lateinit var userLat: String
    private lateinit var userLong: String
    lateinit var searchViewAdapter: SearchViewAdapter
    private lateinit var adapter: ArrayAdapter<Place>
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            locationResult ?: return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map_fragment?.onCreate(savedInstanceState)
        //addmob
        MobileAds.initialize(requireContext()) {}
        loadAd()
        //init map
        initializeMap()
        //init spinner
        setupSpinner()
        //init searchview
        initSearchRV()
        //shareLocation
        share_location.setOnClickListener {
            if (userLat.isNotEmpty()) {
                shareLocation(userLat, userLong, requireActivity())
            }
        }
    }


    private fun initSearchRV() {
        search_result_recyclerView.layoutManager = LinearLayoutManager(requireContext())
        searchViewAdapter = SearchViewAdapter(listdata = placeStatic()) { place ->
            initLocation?.let {
                mMap.setMarkersFromPlace(
                    fragment = this@NearbyFragment,
                    place = place,
                    it
                )
            }
            if (!search_places.isIconified) {
                search_places.isIconified = true
            }
            map_fragment.visibility = View.VISIBLE
            search_result_recyclerView.visibility = View.GONE
        }
        search_result_recyclerView.adapter = searchViewAdapter

        search_places.setOnSearchClickListener {
            map_fragment.visibility = View.GONE
            search_result_recyclerView.visibility = View.VISIBLE
        }

        search_places.setOnCloseListener {
            map_fragment.visibility = View.VISIBLE
            search_result_recyclerView.visibility = View.GONE
            false
        }


        search_places.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText!!.isNotEmpty()) {
                    map_fragment.visibility = View.GONE
                    search_result_recyclerView.visibility = View.VISIBLE
                } else {
                    map_fragment.visibility = View.VISIBLE
                    search_result_recyclerView.visibility = View.GONE
                }
                searchViewAdapter.filter.filter(newText)
                return false
            }
        })


    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            MapsInitializer.initialize(requireContext())
        } catch (e: GooglePlayServicesNotAvailableException) {
            mapsSupported = false

        }

        // Location
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())


    }


    // Map related codes
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        showUserLocation()
        // Add a marker in beirut and move the camera
        val latitude = 33.8938
        val longitude = 35.5018
        val lebLatLong = LatLng(latitude, longitude)
        val zoomLevel = 9.5f
        val pos = initLocation ?: lebLatLong
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel))


    }


    // Location
    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(REQUEST_CODE_FINE_LOCATION)
    private fun showUserLocation() {
        if (EasyPermissions.hasPermissions(requireContext(), ACCESS_FINE_LOCATION)) {
            mMap?.isMyLocationEnabled = true
            val locationRequest = LocationRequest()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = 5000 // 5000ms (5s)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                initLocation = LatLng(it.latitude, it.longitude)
                userLat = it.latitude.toString()
                userLong = it.longitude.toString()
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(initLocation, 10f))
            }

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                host = this,
                rationale = "For showing your current location on the map.",
                requestCode = REQUEST_CODE_FINE_LOCATION,
                perms = *arrayOf(ACCESS_FINE_LOCATION)
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun setupSpinner() {
        val customObjects = placeStatic()
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, customObjects)

        spinner_places.adapter = adapter
        // Item Select Listener
        spinner_places.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(p0: AdapterView<*>?) {
                // You can define you actions as you want
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val selectedObject = spinner_places.selectedItem as Place
                if (clicked) {
                    clicked = true
                    search_result_recyclerView.visibility = View.GONE
                    map_fragment.visibility = View.VISIBLE
                    if (!search_places.isIconified) {
                        search_places.isIconified = true
                    }
                    initLocation?.let {
                        mMap.setMarkersFromPlace(
                            fragment = this@NearbyFragment,
                            place = selectedObject,
                            it
                        )
                    }

                } else {
                    clicked = true
                }
            }
        }
    }


    private fun loadAd() {
        var adRequest = AdRequest.Builder().build()
        adRequest.isTestDevice(requireContext())
        InterstitialAd.load(
            requireContext(),
            //"ca-app-pub-2559564046715323/2204952129", //Check your ad unit ID to make sure it’s created for the right format,"should Add my package to pubspec.yaml file in account"
            "ca-app-pub-3940256099942544/1033173712",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    val error =
                        "domain: ${adError.domain}, code: ${adError.code}, " + "message: ${adError.message}"
                    Log.d(TAG, "Ad was loaded.$error")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                    showInterstitial()
                }
            }
        )
    }

    private fun showInterstitial() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mInterstitialAd = null
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                    mInterstitialAd = null
                }

                override fun onAdShowedFullScreenContent() {
                }
            }
            mInterstitialAd?.show(requireActivity())
        }
    }

    override fun onResume() {
        super.onResume()
        map_fragment.onResume()
        initializeMap()
    }

    override fun onPause() {
        super.onPause()
        map_fragment.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        map_fragment?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        map_fragment?.onLowMemory()
    }

    private fun initializeMap() {
        if (mMap == null && mapsSupported) {
            map_fragment?.getMapAsync(this)
        }
    }

    override fun onDestroyView() {
        mMap = null
        super.onDestroyView()
    }

}





