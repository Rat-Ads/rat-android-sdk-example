package com.example.rat_ads

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class NetworkClient {
    private val client = OkHttpClient()

    fun getAd(requestData: JSONObject): String {
        val body = requestData.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://localhost/api/v1/getAd")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return response.body.string()
    }
}