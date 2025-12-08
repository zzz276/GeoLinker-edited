package com.internetofthings.displaycontroller

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.internetofthings.displaycontroller.databinding.ItemGpsBinding
import com.internetofthings.displaycontroller.models.GPSModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class GPSAdapter(
    private var gpsDataList: List<GPSModel>,
    private val onDeleteClick: (GPSModel) -> Unit
) : RecyclerView.Adapter<GPSAdapter.GPSViewHolder>() {

    class GPSViewHolder(
        private val binding: ItemGpsBinding,
        private val onDeleteClick: (GPSModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("DefaultLocale")
        fun bind(gpsModel: GPSModel) {
            binding.apply {
                tvLatitudeValue.text = String.format("%.6f", gpsModel.latitude)
                tvLongitudeValue.text = String.format("%.6f", gpsModel.longitude)
                tvTimeValue.text = formatTrackedTime(gpsModel.time)

                btnDelete.setOnClickListener { onDeleteClick(gpsModel) }
            }
        }

        private fun formatTrackedTime(raw: String): String {
            val inputPatterns = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
            )

            inputPatterns.forEach { pattern ->
                try {
                    val parsed = LocalDateTime.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
                    val outputFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy 'at' HH:mm:ss", Locale.getDefault())
                    return parsed.format(outputFormatter)
                } catch (_: Exception) {
                    // try next pattern
                }
            }

            // Fallback to original if parsing fails
            return raw
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GPSViewHolder {
        val binding = ItemGpsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return GPSViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: GPSViewHolder, position: Int) { holder.bind(gpsDataList[position]) }

    override fun getItemCount(): Int = gpsDataList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<GPSModel>) {
        gpsDataList = newData

        notifyDataSetChanged()
    }
}

