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
        val email = preferencesManager.getEmail() ?: ""
        val password = preferencesManager.getUserPassword(username) ?: ""

        binding.etEmail.setText(email)
        binding.etPassword.setText(password)
    }

    private fun saveProfile() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        binding.tilEmail.error = null
        binding.tilPassword.error = null

        when {
            email.isEmpty() -> binding.tilEmail.error = "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.tilEmail.error = "Invalid email format"
            password.isEmpty() -> binding.tilPassword.error = "Password is required"
            password.length < 6 -> binding.tilPassword.error = "Password must be at least 6 characters"
            else -> {
                val username = preferencesManager.getUsername() ?: return
                preferencesManager.saveUser(username, password)
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