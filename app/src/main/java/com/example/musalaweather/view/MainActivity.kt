package com.example.musalaweather.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musalaweather.R
import com.example.musalaweather.databinding.ActivityMainBinding
import com.example.musalaweather.utils.placeHolderProgressBar
import com.example.musalaweather.viewmodel.MainViewModel
import com.ferfalk.simplesearchview.SimpleSearchView
import com.ferfalk.simplesearchview.utils.DimensUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var viewModel : MainViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var currentLat : Double? = null
    private var currentLong : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            getCurrentUserLocation()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        getCurrentUserLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        setupSearchView(menu)
        return true
    }

    private fun setupSearchView(menu: Menu) = with(binding) {
        val item = menu.findItem(R.id.action_search)
        searchView.setMenuItem(item)
        searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String): Boolean {
//                Log.e(TAG, getString(R.string.changed, newText))
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                val endPoint = "https://api.openweathermap.org/data/2.5/weather?q=${query}&appid=d19ea649a547add6d64031819080bbe8&units=metric"
                viewModel.getWeatherSearch(endPoint)
                observeLiveData()
                Toast.makeText(this@MainActivity,query,Toast.LENGTH_SHORT).show()
                return false
            }

            override fun onQueryTextCleared(): Boolean {
                return false
            }
        })

    }

    private fun observeLiveData() {
        viewModel.weather.observe(this) { weather ->
            weather?.let {
                if (weather.weather != null) {
                    Glide.with(this).setDefaultRequestOptions(RequestOptions().placeholder(
                        placeHolderProgressBar(applicationContext)
                    )
                        .centerInside()).load("https://openweathermap.org/img/wn/${weather.weather!![0].icon}@4x.png").into(binding.iconImageView)
                    binding.situationTextView.text = weather.weather!![0].situation
                }
                if (weather.dataX != null && weather.dataX?.temp != null) {
                    val celsius = (weather.dataX!!.temp!!).roundToInt()
                    "$celsius°C".also { binding.degreeTextView.text = it
                    }
                    val celsiusTempMin = (weather.dataX!!.temp_min!!).roundToInt()
                    "$celsiusTempMin°C".also { binding.tempMin.text = it
                    }
                    val celsiusTempMax = (weather.dataX!!.temp_max!!).roundToInt()
                    "$celsiusTempMax°C".also { binding.tempMax.text = it
                    }
                    "${weather.dataX?.humidity.toString()} %".also { binding.humidity.text = it }

                    "${weather.wind!!.speed.toString()} km".also { binding.wind.text = it }
                    binding.textCountry.text = weather.country!!.country
                }

            }
            weather.location?.let { location ->
                    binding.locationTextView.text = location

            }
        }

        viewModel.loading.observe(this) { loading ->
            if (loading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.iconImageView.visibility = View.GONE
                binding.degreeTextView.visibility = View.GONE
                binding.situationTextView.visibility = View.GONE
                binding.locationTextView.visibility = View.GONE
                binding.detailsContainer.visibility = View.GONE
                binding.textCountry.visibility = View.GONE

            } else {
                binding.progressBar.visibility = View.GONE
                binding.iconImageView.visibility = View.VISIBLE
                binding.degreeTextView.visibility = View.VISIBLE
                binding.situationTextView.visibility = View.VISIBLE
                binding.locationTextView.visibility = View.VISIBLE
                binding.detailsContainer.visibility = View.VISIBLE
                binding.textCountry.visibility = View.VISIBLE

            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != "") {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
                binding.iconImageView.visibility = View.GONE
                binding.degreeTextView.visibility = View.GONE
                binding.situationTextView.visibility = View.GONE
                binding.locationTextView.visibility = View.GONE

            }
        }
    }
    private fun getCurrentUserLocation() {
        if (checkLocationPermissions()) {
            if (locationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location : Location? = task.result
                    if (location == null) {
                        Toast.makeText(this, "Can't Found Location", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Got Location Successfully", Toast.LENGTH_SHORT).show()
                        currentLat = location.latitude
                        currentLong = location.longitude

                        val endPoint = "data/2.5/weather?lat=${currentLat}&lon=${currentLong}&appid=d19ea649a547add6d64031819080bbe8&units=metric"
                        viewModel.getWeather(endPoint)
                        observeLiveData()
                    }
                }
            } else {
                Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermission()
        }
    }
    private fun checkLocationPermissions() : Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private fun locationEnabled(): Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    companion object {
        private const val PERMISSION_ACCESS_LOCATION_RC = 1
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ACCESS_LOCATION_RC)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_ACCESS_LOCATION_RC) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                getCurrentUserLocation()
            } else {
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}