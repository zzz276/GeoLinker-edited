package com.internetofthings.displaycontroller

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.internetofthings.displaycontroller.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var map: GoogleMap
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        // FAB shows navigation menu
        binding.fab.setOnClickListener { view ->
            showNavigationMenu(view)
        }
    }

    private fun showNavigationMenu(view: android.view.View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.web_navigation_menu, popup.menu)
        
        // Enable/disable menu items based on navigation state
        popup.menu.findItem(R.id.nav_back).isEnabled = map.canGoBack()
        popup.menu.findItem(R.id.nav_forward).isEnabled = map.canGoForward()
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.nav_back -> {
                    if (map.canGoBack()) {
                        map.goBack()
                    } else {
                        Toast.makeText(this, "Cannot go back", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_forward -> {
                    if (map.canGoForward()) {
                        map.goForward()
                    } else {
                        Toast.makeText(this, "Cannot go forward", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.nav_refresh -> {
                    map.reload()
                    true
                }
                R.id.nav_home -> {
                    map.loadUrl("https://www.circuitdigest.cloud/geolinker-web-app?device_id=My+Tracker")
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
            • ${getString(R.string.help_nav_refresh)}
            • ${getString(R.string.help_nav_home)}
            
            ${getString(R.string.help_tips)}
            • ${getString(R.string.help_tip_1)}
            • ${getString(R.string.help_tip_2)}
            • ${getString(R.string.help_tip_3)}
            • ${getString(R.string.help_tip_4)}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(R.string.help_title)
            .setMessage(helpMessage)
            .setPositiveButton(R.string.got_it) { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
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
        val sydney = LatLng(-34.0, 151.0)

        map.addMarker(MarkerOptions().position(sydney).title("Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}