package com.example.finance.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finance.R
import com.example.finance.data.manager.PreferencesManager
import com.example.finance.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        preferencesManager = PreferencesManager(requireContext())

        // Load current profile data
        loadProfile()

        // Set up buttons
        binding.btnSave.setOnClickListener { saveProfile() }
        binding.btnCancel.setOnClickListener {
            Toast.makeText(context, "Changes discarded", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
        }

        return binding.root
    }

    private fun loadProfile() {
        val username = preferencesManager.getUsername() ?: ""
        val fullName = preferencesManager.getUserFullName(username) ?: ""
        val email = preferencesManager.getEmail() ?: ""
        val phone = preferencesManager.getUserPhone(username) ?: ""
        val address = preferencesManager.getUserAddress(username) ?: ""

        binding.etUsername.setText(username)
        binding.etFullName.setText(fullName)
        binding.etEmail.setText(email)
        binding.etPhone.setText(phone)
        binding.etAddress.setText(address)
        binding.etUsername.isEnabled = false // Username cannot be edited
    }

    private fun saveProfile() {
        val username = preferencesManager.getUsername() ?: ""
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()

        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPhone.error = null
        binding.tilAddress.error = null

        when {
            fullName.isEmpty() -> binding.tilFullName.error = "Full name is required"
            email.isEmpty() -> binding.tilEmail.error = "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.tilEmail.error = "Invalid email format"
            phone.isEmpty() -> binding.tilPhone.error = "Phone number is required"
            address.isEmpty() -> binding.tilAddress.error = "Address is required"
            else -> {
                preferencesManager.saveUserDetails(username, fullName, email, phone, address)
                preferencesManager.setEmail(email)
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editProfileFragment_to_profileFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}