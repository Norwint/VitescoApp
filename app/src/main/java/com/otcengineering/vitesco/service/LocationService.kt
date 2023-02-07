package com.otcengineering.vitesco.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.otcengineering.vitesco.data.Coordinate
import java.util.*


class LocationService private constructor(ctx: Context) : LocationListener {
    companion object {
        private lateinit var service: LocationService
        fun getService(ctx: Context): LocationService {
            if (!this::service.isInitialized) {
                service = LocationService(ctx)
            }

            return service
        }

        fun getService(): LocationService {
            return service
        }
    }

    private var location: Coordinate = Coordinate(0.0, 0.0)

    init {
        val locationService = ctx.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationService.requestLocationUpdates(LocationManager.FUSED_PROVIDER, 5000, 10.0F, this)
            } else {
                locationService.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10.0F, this)
            }
            val pt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                locationService.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            } else {
                locationService.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (pt != null) {
                this.location = Coordinate(pt.latitude, pt.longitude)
            }
        }
    }

    override fun onLocationChanged(p0: Location) {
        location = Coordinate(p0.latitude, p0.longitude)
    }

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    fun getLocation() = location
}