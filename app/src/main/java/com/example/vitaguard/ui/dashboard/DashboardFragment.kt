package com.example.vitaguard.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.example.vitaguard.R
import com.example.vitaguard.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textStatus: TextView = binding.textStatus
        val imageStatus: ImageView = binding.imageStatus
        /*
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textStatus.text = it
            textStatus.setTextColor(Color.RED)

        }
        imageStatus.setImageResource(R.drawable.red_heart)
        */
        val healthData: Bundle
        updateReport()

        return root
    }

    private fun updateReport() {
        setFragmentResultListener("health") {requestKey, bundle ->
            val bp = bundle.getDouble("bps").toString() + " / " + bundle.getDouble("bpd").toString()
            val hr = bundle.getDouble("hr")
            val bo = bundle.getDouble("bo")
            val color = bundle.getString("color")
            //Log.d("health", "bp: " + bp)
            //Log.d("health", "hr: " + hr)
            //Log.d("health", "bo: " + bo)
            //Log.d("health", "color: " + color)
            binding.textBloodPressure.text = getString(R.string.dash_bp) + " " + bp
            binding.textHeartRate.text = getString(R.string.dash_hr) + " " + hr
            binding.textOxygen.text = getString(R.string.dash_bo) + " " + bo

            when (color) {
                "r" -> {
                    binding.textStatus.setTextColor(Color.RED)
                    binding.textStatus.text = "Bad Health"
                    binding.imageStatus.setImageResource(R.drawable.red_heart)
                }
                "y" -> {
                    binding.textStatus.setTextColor(Color.YELLOW)
                    binding.textStatus.text = "Mid Health"
                    binding.imageStatus.setImageResource(R.drawable.yellow_heart)
                }
                "g" -> {
                    binding.textStatus.setTextColor(Color.GREEN)
                    binding.textStatus.text = "Good Health"
                    binding.imageStatus.setImageResource(R.drawable.green_heart)
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}