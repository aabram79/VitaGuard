package com.example.vitaguard.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.vitaguard.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val changeData1: TextView = binding.listOutput1
        val changeData2: TextView = binding.listOutput2
        val changeData3: TextView = binding.listOutput3

        fun iterateUsingListIterator1() {
            val myList = mutableListOf("A", "B", "C", "D", "E")
            val iterator = myList.listIterator()
            while (iterator.hasNext()) {
                val displayNext1 = iterator.next()
                changeData1.text = displayNext1
            }
        }

        fun iterateUsingListIterator2() {
            val myList = mutableListOf("A", "B", "C", "D", "E")
            val iterator = myList.listIterator()
            while (iterator.hasNext()) {
                val displayNext1 = iterator.next()
                changeData2.text = displayNext1
            }
        }

        fun iterateUsingListIterator3() {
            val myList = mutableListOf("A", "B", "C", "D", "E")
            val iterator = myList.listIterator()
            while (iterator.hasNext()) {
                val displayNext1 = iterator.next()
                changeData3.text = displayNext1
            }
        }

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}