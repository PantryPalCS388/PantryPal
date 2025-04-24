package com.example.pantrypal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var tvUserEmail: TextView
    private lateinit var btnSignOut: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        btnSignOut = view.findViewById(R.id.btnSignOut)

        // Set the user's email
        val user = auth.currentUser
        tvUserEmail.text = "Email: ${user?.email ?: "Not signed in"}"

        // Sign out on click
        btnSignOut.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}
