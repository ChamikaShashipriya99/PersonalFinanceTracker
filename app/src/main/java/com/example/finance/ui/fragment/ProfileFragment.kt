package com.example.finance.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        preferencesManager = PreferencesManager(requireContext())

        // Load profile data
        loadProfile()

        // Set up edit profile button
        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        return binding.root
    }

    private fun loadProfile() {
        val username = preferencesManager.getUsername() ?: ""
        val email = preferencesManager.getEmail() ?: ""
        val password = preferencesManager.getUserPassword(username) ?: ""

        binding.etUsername.setText(username)
        binding.etEmail.setText(email)
        binding.etPassword.setText(password)
        binding.etUsername.isEnabled = false // Username cannot be edited
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}