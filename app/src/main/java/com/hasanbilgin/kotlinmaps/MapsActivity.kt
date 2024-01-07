package com.hasanbilgin.kotlinmaps


import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.hasanbilgin.kotlinmaps.databinding.ActivityMapsBinding

//GoogleMap.OnMapClickListener uzun tıklama için kondu her tıklamada aolan metotda içinde
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    //konum yöneticisi
    private lateinit var locationManager: LocationManager

    //konum dinleyicisi
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher();
        sharedPreferences = this.getSharedPreferences("com.hasanbilgin.kotlinmaps", MODE_PRIVATE)
        trackBoolean = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0

    }

    //harita hazır olduğunda çalışan metot
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //uzun tıklanma için
        mMap.setOnMapLongClickListener(this)


//        // Add a marker in Sydney and move the camera
//        //latitude =enlem | longitude =boylam
////        val sydney = LatLng(-34.0, 151.0)
////        //işaretçi (marker) ekle sydneye ekle ismi ne Marker in Sydney
////        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
////        //ekran direk gelmesi
////        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//        val eiffel = LatLng(48.85391, 2.2913515)
//        //değişik işaretler mevcuttur
//        mMap.addMarker(MarkerOptions().position(eiffel).title("Eiffel Tower"))
////        mMap.moveCamera(CameraUpdateFactory.newLatLng(eiffel))
//        //zoomlu
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eiffel, 15f))

        //similasyon programında dışındaki 3 noktaya tıklarsan Locationda Single pointste harita biyeretıkla ve set location de ora şimdlik konumun olur/ telefondna anki konumu yapmak içinse fake gps andrid uygulamalardan yapılabilir

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

//        locationListener= LocationListener {location->
//            //it güncel veri yada isimde verilebilir location-> gibi
//            //location.altitude//yükseklik  .speed // hız . time //zaman
//            //bunu kullanmadık
//        }
        locationListener = object : LocationListener {
            //konum değiştiğinde konum veren metot
            override fun onLocationChanged(location: Location) {
//                println("location: "+location.toString())
                trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
//                if (!trackBoolean!!) {//aynı
                if (trackBoolean == false) {
                    //enlem boylam verildi
                    val userLocation = LatLng(location.latitude, location.longitude);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                }


            }
//            //bize bu konumu sağlayan birşey etkin olursa
//            override fun onProviderEnabled(provider: String) {
//                super.onProviderEnabled(provider)
//            }
//            //devre dışı kalırsa
//            override fun onProviderDisabled(provider: String) {
//                super.onProviderDisabled(provider)
//            }
//            //genel olarak durum değişirse
//            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
//                super.onStatusChanged(provider, status, extras)
//            }
        }
        //1000/*1sn de bir*/,10/*10 metrede 1*/


        //locationManager.getCuurentLocation güncel konumu al
        //locationManager.getLastKnowLocation son bilinen konumu al
        //locationManager.getProvider() //konum sağlayıcı kimse bul
        //locationManager.requestLocationUpdates()//konum değiştikçe güncellemelerini iste
        //permission -> android kütüphanesindne alındı
        //Manifestte izin verildiğini kontrol eder izin verilmediyse demektir
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Snackbar.make(
                    binding.root,
                    "Permission needed for location",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Give Permission") {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            } else {
                //request permission
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        } else {
            //permission granted
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0f,
                locationListener
            )
            //en son locasyonu almak için
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
            }
            //konumumu etkinleştirdikmi true diyerek etkinleştirdik demek mavi tik verdi //sadece iznimiz varsa yapabiliyoruz
            mMap.isMyLocationEnabled = true
        }
    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    //permission grandted
                    //
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //bu illa üstteki ifi istyo
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            1000,
                            0f,
                            locationListener
                        )
                        //en son locasyonu almak için
                        val lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val lastUserLocation =
                                LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastUserLocation,
                                    15f
                                )
                            )
                        }
                        //konumumu etkinleştirdikmi true diyerek etkinleştirdik demek mavi tik verdi //sadece iznimiz varsa yapabiliyoruz
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    //permission denied
                    Toast.makeText(this@MapsActivity, "Permission needed", Toast.LENGTH_LONG).show()
                }
            }

    }

    //açılır
    override fun onMapLongClick(p0: LatLng) {
        //marker silicektir
        mMap.clear()

        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude=p0.latitude
        selectedLongitude=p0.longitude
    }
}