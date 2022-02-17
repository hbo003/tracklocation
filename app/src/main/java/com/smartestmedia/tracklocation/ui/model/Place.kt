package com.smartestmedia.tracklocation.ui.model

import com.google.android.gms.maps.model.LatLng

data class Place(
    val name: String,
    val latLng: LatLng,
    val closeoropen: String,
    val closedAt: String,
    val imag: String,
) {
    override fun toString(): String {
        return name
    }
}


fun placeStatic(): ArrayList<Place> {
    val mList = arrayListOf<Place>()
    val place1 = Place(
        "Grand Kadri Hotel",
        LatLng(33.85148430277257, 35.895525763213946),
        "Open",
        "Closed at 23:00 ",
        "c"
    )
    val place2 = Place(
        "Germanos - Pastry",
        LatLng(33.85217073479985, 35.89477838111461),
        "Open",
        "Closed at 21:00 ",
        "c"
    )
    val place3 = Place(
        "Malak el Tawook",
        LatLng(33.85334017189446, 35.89438946093824),
        "Open",
        "Closed at 20:00 ",
        "c"
    )
    val place4 = Place(
        "Z Burger House",
        LatLng(33.85454300475094, 35.894561122304474),
        "Closed",
        "Closed at 22:00 ",
        "c"
    )
    val place5 = Place(
        "Collège Oriental",
        LatLng(33.85129821373707, 35.89446263654391),
        "Open",
        "Closed at 21:00 ",
        "c"
    )
    val place6 = Place(
        "VERO MODA",
        LatLng(33.85048738635312, 35.89664059012788),
        "Closed",
        "Closed at 21:00 ",
        "c"
    )
    mList.apply {
        add(place1)
        add(place2)
        add(place3)
        add(place4)
        add(place5)
        add(place6)
    }

    return mList
}