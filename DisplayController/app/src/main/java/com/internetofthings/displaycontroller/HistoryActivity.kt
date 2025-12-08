package com.internetofthings.displaycontroller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.internetofthings.displaycontroller.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var gpsAdapter: GPSAdapter
    private lateinit var gpsDataManager: GPSDataManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.history)

        // Initialize GPS data manager
        gpsDataManager = GPSDataManager(this)

        // Set up RecyclerView with empty list initially
        gpsAdapter = GPSAdapter(emptyList()) { gpsModel -> deleteGPSData(gpsModel) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = gpsAdapter
        }

        // Handle back button
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        // Load GPS data from database
        loadGPSData()
    }

    private fun loadGPSData() {
        lifecycleScope.launch {
            val gpsData = gpsDataManager.getAllGPSData()
            gpsAdapter.updateData(gpsData)
            updateEmptyState(gpsData.isEmpty())
        }
    }

    private fun deleteGPSData(gpsModel: com.internetofthings.displaycontroller.models.GPSModel) {
        lifecycleScope.launch {
            gpsDataManager.deleteGPSData(gpsModel)
            val updatedData = gpsDataManager.getAllGPSData()
            gpsAdapter.updateData(updatedData)
            updateEmptyState(updatedData.isEmpty())
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.recyclerView.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
        binding.tvEmpty.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to activity
        loadGPSData()
    }
}

