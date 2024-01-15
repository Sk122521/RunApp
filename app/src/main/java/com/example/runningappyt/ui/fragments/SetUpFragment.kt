package com.example.runningappyt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningappyt.R
import com.example.runningappyt.databinding.FragmentSetUpBinding
import com.example.runningappyt.ui.viewmodels.MainViewModel
import com.example.runningappyt.utils.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningappyt.utils.Constants.KEY_NAME
import com.example.runningappyt.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetUpFragment : Fragment() {

    private var _binding : FragmentSetUpBinding? = null
    private val binding  get()   = _binding!!

    private val viewModel : MainViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstRun  = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isFirstRun){
            val navOption  = NavOptions.Builder()
                .setPopUpTo(R.id.setUpFragment,true)
                .build()
            findNavController().navigate(
                R.id.action_setUpFragment_to_runFragment,
                savedInstanceState,
                navOption
            )
        }


        binding.tvContinue.setOnClickListener {
            val success  = writePersonalDataToSharedPreferences()
            if(success){
                findNavController().navigate(R.id.action_setUpFragment_to_runFragment)
            }else{
                Snackbar.make(requireView(),"Please Enter all the fields", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSetUpBinding.inflate(inflater,container,false)


        return binding.root
    }

    private fun writePersonalDataToSharedPreferences() : Boolean{
          val name = binding.etName.text.toString()
          val weight  = binding.etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
        val toolbarText = "Let's go , $name"
       requireActivity().apply {
           this.findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolbarText
       }
        return true
    }
}