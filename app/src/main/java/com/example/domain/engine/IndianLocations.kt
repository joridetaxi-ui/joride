package com.example.domain.engine

import com.example.data.model.LocationPoint

object IndianLocations {
    val POPULAR_HOTSPOTS = listOf(
        LocationPoint(
            title = "Indiranagar 100ft Road",
            address = "12th Main, Indiranagar, Bengaluru, KA",
            lat = 12.9784,
            lng = 77.6408
        ),
        LocationPoint(
            title = "Koramangala 5th Block",
            address = "Sony World Signal, 80ft Road, Bengaluru, KA",
            lat = 12.9352,
            lng = 77.6245
        ),
        LocationPoint(
            title = "MG Road Metro Station",
            address = "Mahatma Gandhi Rd, Bengaluru, KA",
            lat = 12.9756,
            lng = 77.6066
        ),
        LocationPoint(
            title = "HSR Layout Sector 1",
            address = "27th Main Rd, HSR Layout, Bengaluru, KA",
            lat = 12.9121,
            lng = 77.6446
        ),
        LocationPoint(
            title = "Whitefield Tech Park",
            address = "ITPL Main Rd, Whitefield, Bengaluru, KA",
            lat = 12.9866,
            lng = 77.7377
        ),
        LocationPoint(
            title = "Krantivira Sangolli Rayanna (Majestic)",
            address = "Railway Station, Gubbi Thotadappa Rd, Bengaluru",
            lat = 12.9783,
            lng = 77.5694
        ),
        LocationPoint(
            title = "Electronic City Phase 1",
            address = "Hosur Rd, Electronic City, Bengaluru, KA",
            lat = 12.8452,
            lng = 77.6602
        ),
        LocationPoint(
            title = "Jayanagar 4th Block Complex",
            address = "11th Main, Jayanagar, Bengaluru, KA",
            lat = 12.9299,
            lng = 77.5824
        ),
        LocationPoint(
            title = "Kempegowda International Airport",
            address = "KIAL Rd, Devanahalli, Bengaluru, KA",
            lat = 13.1986,
            lng = 77.7066
        )
    )

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth's radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val dist = r * c
        return if (dist < 0.8) 1.2 else Math.round(dist * 10.0) / 10.0
    }

    fun estimateDurationMinutes(distanceKm: Double, speedKmH: Double = 26.0): Int {
        val mins = (distanceKm / speedKmH * 60).toInt()
        return if (mins < 5) 6 else mins
    }
}
