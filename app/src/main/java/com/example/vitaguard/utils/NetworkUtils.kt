package com.example.vitaguard.utils

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

object NetworkUtils {

    private const val TAG = "NetworkUtils"
    private var requestSuccess = false
    fun sendVolleyPostRequest(context: Context, url: String, postMessage: String): Boolean {

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            Response.Listener { response ->
                Log.d(TAG, "Line 22 Response: $response")
                requestSuccess = true
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "Line 26 Error")
                Log.e(TAG, "Error: ${error.networkResponse?.statusCode}, ${error.message}")
                requestSuccess = false
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["message"] = postMessage
                Log.d(TAG, "Params: $params")
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                Log.d(TAG, "Headers: $headers")
                return headers
            }

            override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
                if (volleyError?.networkResponse != null) {
                    val statusCode = volleyError.networkResponse.statusCode
                    val data = volleyError.networkResponse.data?.let { String(it) }
                    Log.e(TAG, "NetworkError: statusCode=$statusCode, data=$data")
                }
                return super.parseNetworkError(volleyError)
            }
        }

        requestQueue.add(stringRequest)
        Log.d(TAG, "requestSuccess result is $requestSuccess")
        return requestSuccess
    }
}
