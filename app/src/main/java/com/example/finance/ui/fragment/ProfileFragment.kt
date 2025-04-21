package com.example.finance.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide

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

        // Set up logout button
        binding.btnLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    private fun loadProfile() {
        val username = preferencesManager.getUsername() ?: ""
        val fullName = preferencesManager.getUserFullName(username) ?: ""
        val email = preferencesManager.getEmail() ?: ""
        val phone = preferencesManager.getUserPhone(username) ?: ""
        val address = preferencesManager.getUserAddress(username) ?: ""

        // Load profile photo
        preferencesManager.getProfilePhoto(username)?.let { photoUri ->
            Glide.with(this)
                .load(Uri.parse(photoUri))
                .circleCrop()
                .into(binding.ivProfilePhoto)
        }

        binding.etUsername.setText(username)
        binding.etFullName.setText(fullName)
        binding.etEmail.setText(email)
        binding.etPhone.setText(phone)
        binding.etAddress.setText(address)
        binding.etUsername.isEnabled = false // Username cannot be edited
    }

    private fun logout() {
        preferencesManager.setLoggedIn(false)
        preferencesManager.setUsername("")
        preferencesManager.setEmail("")
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}