package com.example.btl_mobile

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class ChonViTri_Map : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tvAddress: TextView
    private lateinit var etSearch: EditText
    private var selectedLatLng: LatLng? = null
    private var selectedAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_picker)

        webView = findViewById(R.id.webViewMap)
        tvAddress = findViewById(R.id.tv_picked_address)
        etSearch = findViewById(R.id.etSearchMap)
        val btnSearch = findViewById<ImageButton>(R.id.btnSearchMap)
        val btnConfirm = findViewById<Button>(R.id.btn_confirm_location)

        setupWebView()

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString()
            if (query.isNotEmpty()) {
                searchLocation(query)
            }
        }

        btnConfirm.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("lat", selectedLatLng?.latitude ?: 0.0)
            resultIntent.putExtra("lng", selectedLatLng?.longitude ?: 0.0)
            resultIntent.putExtra("address", selectedAddress)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun setupWebView() {
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Initial position
                val initialLat = intent.getDoubleExtra("initial_lat", 21.0285)
                val initialLng = intent.getDoubleExtra("initial_lng", 105.8542)
                updateMapPosition(initialLat, initialLng)
            }
        }

        // Load a simple HTML map using Leaflet (No API Key required, looks like HTML map)
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    #map { height: 100vh; width: 100vw; margin: 0; padding: 0; }
                    .leaflet-control-attribution { display: none; }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var map = L.map('map').setView([21.0285, 105.8542], 15);
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                    
                    map.on('moveend', function() {
                        var center = map.getCenter();
                        window.android.onMoveEnd(center.lat, center.lng);
                    });

                    function setView(lat, lng) {
                        map.setView([lat, lng], 17);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.addJavascriptInterface(object {
            @android.webkit.JavascriptInterface
            fun onMoveEnd(lat: Double, lng: Double) {
                runOnUiThread {
                    selectedLatLng = LatLng(lat, lng)
                    updateAddress(selectedLatLng!!)
                }
            }
        }, "android")

        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun updateMapPosition(lat: Double, lng: Double) {
        webView.evaluateJavascript("setView($lat, $lng)", null)
    }

    private fun searchLocation(query: String) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(query, 1)
            if (addresses?.isNotEmpty() == true) {
                val addr = addresses[0]
                updateMapPosition(addr.latitude, addr.longitude)
                etSearch.clearFocus()
            } else {
                Toast.makeText(this, "Không tìm thấy địa điểm", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Lỗi tìm kiếm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAddress(latLng: LatLng) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                selectedAddress = addresses[0].getAddressLine(0)
                tvAddress.text = selectedAddress
            } else {
                tvAddress.text = "Không tìm thấy địa chỉ"
            }
        } catch (e: Exception) {
            tvAddress.text = "Lỗi khi lấy địa chỉ"
        }
    }
}