package com.example.finance.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.finance.R
import com.example.finance.databinding.FragmentRegisterBinding
import com.example.finance.data.manager.PreferencesManager
import com.bumptech.glide.Glide

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferencesManager: PreferencesManager
    private var selectedPhotoUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.ivProfilePhoto)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        preferencesManager = PreferencesManager(requireContext())

        binding.btnRegister.setOnClickListener { validateAndRegister() }
        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.btnSelectPhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        return binding.root
    }

    private fun validateAndRegister() {
        val username = binding.etUsername.text.toString().trim()
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        binding.tilUsername.error = null
        binding.tilFullName.error = null
        binding.tilEmail.error = null
        binding.tilPhone.error = null
        binding.tilAddress.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        when {
            username.isEmpty() -> binding.tilUsername.error = "Username is required"
            fullName.isEmpty() -> binding.tilFullName.error = "Full name is required"
            email.isEmpty() -> binding.tilEmail.error = "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.tilEmail.error = "Invalid email format"
            phone.isEmpty() -> binding.tilPhone.error = "Phone number is required"
            address.isEmpty() -> binding.tilAddress.error = "Address is required"
            password.isEmpty() -> binding.tilPassword.error = "Password is required"
            confirmPassword.isEmpty() -> binding.tilConfirmPassword.error = "Confirm password is required"
            password.length < 6 -> binding.tilPassword.error = "Password must be at least 6 characters"
            password != confirmPassword -> binding.tilConfirmPassword.error = "Passwords do not match"
            preferencesManager.getUserPassword(username) != null -> binding.tilUsername.error = "Username already exists"
            else -> {
                preferencesManager.saveUser(username, password)
                preferencesManager.saveUserDetails(username, fullName, email, phone, address)
                selectedPhotoUri?.let { uri ->
                    preferencesManager.saveProfilePhoto(username, uri.toString())
                }
                preferencesManager.setUsername(username)
                preferencesManager.setEmail(email)
                Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}