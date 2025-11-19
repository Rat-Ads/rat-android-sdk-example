package com.example.rat_ads

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.util.Locale

object DeviceInfoCollector {
    fun collect(context: Context): JSONObject {
        val info = JSONObject()
        info.put("device_model", Build.MODEL)
        info.put("os_version", Build.VERSION.RELEASE)
        info.put("locale", Locale.getDefault().toString())
        info.put("package", context.packageName)
        Log.d("DeviceInfoCollector", "Collected device info: $info")
        checkLocationPermission(context);
        return info
    }

    fun checkLocationPermission(context: Context){
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (hasLocationPermission) {
            Log.d("Permissions", "We already have ACCESS_FINE_LOCATION")
        } else {
            Log.d("Permissions", "We do NOT have ACCESS_FINE_LOCATION")
        }

    }
}