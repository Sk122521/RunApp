package com.example.runningappyt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.runningappyt.R
import com.example.runningappyt.databinding.FragmentSetUpBinding
import com.example.runningappyt.databinding.FragmentSettingsBinding
import com.example.runningappyt.ui.viewmodels.MainViewModel
import com.example.runningappyt.utils.Constants
import com.example.runningappyt.utils.Constants.KEY_NAME
import com.example.runningappyt.utils.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {


    private var _binding : FragmentSettingsBinding? = null
    private val binding  get()   = _binding!!

    private val viewModel : MainViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFieldFromSharedPrefs()
        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPreferences()
            if(success){
                Snackbar.make(view,"Saved Changes",Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view,"Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    private fun loadFieldFromSharedPrefs(){
        val name = sharedPreferences.getString(KEY_NAME,"")
        val weight  = sharedPreferences.getString(KEY_WEIGHT,"")
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPreferences() : Boolean{
        val name = binding.etName.text.toString()
        val weight  = binding.etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME,name)
            .putFloat(Constants.KEY_WEIGHT,weight.toFloat())
            .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE,false)
        val toolbarText = "Let's go , $name"
        requireActivity().apply {
            this.findViewById<MaterialTextView>(R.id.tvToolbarTitle).text = toolbarText
        }
        return true
    }
}