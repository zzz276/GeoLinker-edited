package com.internetofthings.displaycontroller

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.room.Room
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.internetofthings.displaycontroller.databinding.ActivityHomeBinding
import com.internetofthings.displaycontroller.helpers.Database
import com.internetofthings.displaycontroller.models.GPSModel
import com.internetofthings.displaycontroller.websocket.GPSWebSocketClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var map: GoogleMap
    private lateinit var sessionManager: SessionManager
    private lateinit var wsClient: GPSWebSocketClient
    private lateinit var db: Database

    private val pathPoints = mutableListOf<LatLng>()
    private var polyline: Polyline? = null
    private var marker: Marker? = null
    private var currentMarkerIndex: Int = -1 // -1 means at the latest marker
    private var isMapReady: Boolean = false
    private val pendingHistoricalPoints = mutableListOf<GPSModel>()
    private val pendingLiveUpdates = mutableListOf<GPSModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Initialize Room database
        db = Room.databaseBuilder(
            applicationContext,
            Database::class.java,
            "my_gps_db"
        ).build()

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

        mapFragment?.getMapAsync(this)

        // FAB shows navigation menu
        binding.fab.setOnClickListener { view -> showNavigationMenu(view) }

        // Zoom In FAB
        binding.fabZoomIn.setOnClickListener { zoomIn() }

        // Zoom Out FAB
        binding.fabZoomOut.setOnClickListener { zoomOut() }

        // History FAB
        binding.fabHistory.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)

            startActivity(intent)
        }

        // Load historical points before connecting live
        GlobalScope.launch {
            val oldPoints = db.gpsDAO().getAllGPS()

            runOnUiThread {
                pendingHistoricalPoints.clear()
                pendingHistoricalPoints.addAll(oldPoints)
                applyPendingUpdatesIfReady()
            }
        }

        wsClient = GPSWebSocketClient("10.189.170.45", db.gpsDAO()) { gpsData -> runOnUiThread { updateMap(gpsData) }}

        wsClient.connect()
    }

    private fun showNavigationMenu(view: android.view.View) {
        val popup = PopupMenu(this, view)

        popup.menuInflater.inflate(R.menu.web_navigation_menu, popup.menu)
        
        // Enable/disable menu items based on navigation state
        val canGoBack = pathPoints.isNotEmpty() && currentMarkerIndex > 0
        val canGoForward = pathPoints.isNotEmpty() && currentMarkerIndex < pathPoints.size - 1
        
        popup.menu.findItem(R.id.nav_back).isEnabled = canGoBack
        popup.menu.findItem(R.id.nav_forward).isEnabled = canGoForward
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_back -> {
                    navigateToPreviousMarker()
                    true
                }
                R.id.nav_forward -> {
                    navigateToNextMarker()
                    true
                }
                R.id.nav_help -> {
                    showHelpDialog()
                    true
                }
                R.id.nav_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }

    private fun showHelpDialog() {
        val helpMessage = """
            ${getString(R.string.help_welcome)}
            
            ${getString(R.string.help_getting_started)}
            • ${getString(R.string.help_device_id)}
            • ${getString(R.string.help_view_map)}
            • ${getString(R.string.help_interact)}
            
            ${getString(R.string.help_features)}
            • ${getString(R.string.help_real_time)}
            • ${getString(R.string.help_history)}
            • ${getString(R.string.help_settings)}
            
            ${getString(R.string.help_navigation)}
            ${getString(R.string.help_fab_menu)}
            • ${getString(R.string.help_nav_back)}
            • ${getString(R.string.help_nav_forward)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(R.string.help_title)
            .setMessage(helpMessage)
            .setPositiveButton(R.string.got_it) { dialog, _ -> dialog.dismiss() }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun zoomIn() {
        if (::map.isInitialized) { map.animateCamera(CameraUpdateFactory.zoomIn()) }
        else { Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show() }
    }

    private fun zoomOut() {
        if (::map.isInitialized) { map.animateCamera(CameraUpdateFactory.zoomOut()) }
        else { Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show() }
    }

    private fun navigateToPreviousMarker() {
        if (pathPoints.isEmpty()) {
            Toast.makeText(this, "No markers available", Toast.LENGTH_SHORT).show()
            return
        }
        
        // If at latest marker (-1), go to last index
        if (currentMarkerIndex == -1) {
            currentMarkerIndex = pathPoints.size - 1
        } else if (currentMarkerIndex > 0) {
            currentMarkerIndex--
        } else {
            Toast.makeText(this, "Already at the first marker", Toast.LENGTH_SHORT).show()
            return
        }
        
        navigateToMarker(currentMarkerIndex)
    }

    private fun navigateToNextMarker() {
        if (pathPoints.isEmpty()) {
            Toast.makeText(this, "No markers available", Toast.LENGTH_SHORT).show()
            return
        }
        
        // If at latest marker (-1), can't go forward
        if (currentMarkerIndex == -1) {
            Toast.makeText(this, "Already at the latest marker", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (currentMarkerIndex < pathPoints.size - 1) {
            currentMarkerIndex++
            navigateToMarker(currentMarkerIndex)
        } else {
            // Reached the latest marker
            currentMarkerIndex = -1
            navigateToMarker(pathPoints.size - 1)
        }
    }

    private fun navigateToMarker(index: Int) {
        if (index < 0 || index >= pathPoints.size) return
        
        val targetLocation = pathPoints[index]
        
        // Update marker position if it exists
        marker?.position = targetLocation
        
        // Animate camera to the marker
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 15.0f))
    }

    private fun performLogout() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout) { _, _ ->
                // Clear login state
                sessionManager.logout()
                
                // Navigate back to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        isMapReady = true
        applyPendingUpdatesIfReady()
    }

    private fun updateMap(gpsData: GPSModel) {
        if (!isMapReady) {
            pendingLiveUpdates.add(gpsData)
            return
        }

        applyLiveUpdate(gpsData)
    }

    private fun applyLiveUpdate(gpsData: GPSModel) {
        val location = LatLng(gpsData.latitude, gpsData.longitude)
        pathPoints.add(location)

        // Update marker
        if (marker == null) { 
            marker = map.addMarker(MarkerOptions().position(location).title("My Tracker").snippet("Tracked: ${gpsData.time}")) 
        } else { 
            marker?.position = location 
        }

        // Update polyline with new points
        polyline?.points = pathPoints

        // Reset to latest marker when new data arrives
        currentMarkerIndex = -1

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f))
    }

    private fun drawHistoricalPath(oldPoints: List<GPSModel>) {
        if (!isMapReady || oldPoints.isEmpty()) return

        // Convert DB points to LatLng
        pathPoints.addAll(oldPoints.map { LatLng(it.latitude, it.longitude) })

        // Draw polyline
        polyline = map.addPolyline(
            PolylineOptions()
                .addAll(pathPoints)
                .color(Color.BLUE) // historical path in blue
                .width(5.0f)
        )

        // Place marker at last historical point
        val lastPoint = pathPoints.last()
        val lastTime = oldPoints.lastOrNull()?.time ?: ""
        marker = map.addMarker(MarkerOptions().position(lastPoint).title("My Tracker").snippet("Last tracked: $lastTime"))

        // Set current index to latest marker
        currentMarkerIndex = -1

        // Center camera on last historical point
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 15.0f))
    }

    private fun applyPendingUpdatesIfReady() {
        if (!isMapReady) return

        if (pendingHistoricalPoints.isNotEmpty()) {
            drawHistoricalPath(pendingHistoricalPoints.toList())
            pendingHistoricalPoints.clear()
    }

        if (pendingLiveUpdates.isNotEmpty()) {
            pendingLiveUpdates.forEach { applyLiveUpdate(it) }
            pendingLiveUpdates.clear()
        }
    }
}

