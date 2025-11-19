package com.example.rat_ads

import android.content.Context
import android.util.Log

object RatAdsSdk {
    fun initialize(context: Context, apiKey: String) {
        // store key, set up network client
        Log.d("RatAdsSdk", "Initializing RatAdsSdk with API key: $apiKey")
        DeviceInfoCollector.collect(context)
    }
}
