package com.jasoncsullivan.yelpclone

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException


private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"

class MainActivity : AppCompatActivity(), ConnectivityReceiver.ConnectivityReceiverListener {

    private val restaurants = mutableListOf<YelpRestaurant>()
    private val adapter = RestaurantsAdapter(this, restaurants)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // handle connectivity
        registerReceiver(ConnectivityReceiver(), IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        setContentView(R.layout.activity_main)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)

        if (isOnline()) {
            displayStuff()
        } else {
            showConnectivityAlert(false)
        }
    }

    private fun displayStuff() {
        val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val yelpService = retrofit.create(YelpService::class.java)
        yelpService.searchRestaurants("Bearer $API_KEY","Avocado Toast", "New York").enqueue(object: Callback<YelpSearchResult> {

            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onResponse: $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG,"Did not receive valid response body from Yelp API...exiting")
                    return
                }
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }
            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {
                Log.i(TAG, "onFailure: $t")
            }

        })
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Log.i(TAG, "Network connection changed...")
        showConnectivityAlert(isConnected)
    }

    private fun showConnectivityAlert(isConnected: Boolean) {
        val snackbar = Snackbar.make(rvRestaurants, "Please connect to the internet.", Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        snackbar.setActionTextColor(
            ContextCompat.getColor(this, android.R.color.white))
        if (!isConnected) {
            Log.i(TAG, "Not connected to the internet...")
            snackbar.show()
        } else {
            Log.i(TAG, "Connected!")
            displayStuff()
            snackbar.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }



}



