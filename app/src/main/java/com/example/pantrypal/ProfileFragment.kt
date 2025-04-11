package com.example.pantrypal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val signOutButton = view.findViewById<Button>(R.id.btnSignOut)
        signOutButton.setOnClickListener {
            // Add your sign-out logic here
        }

        return view
    }
}
