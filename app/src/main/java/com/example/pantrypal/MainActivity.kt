package com.example.pantrypal

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
class MainActivity : AppCompatActivity() {

    lateinit var pantryFragment: PantryFragment
    lateinit var profileFragment: ProfileFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        pantryFragment = PantryFragment()
        profileFragment = ProfileFragment()

        replaceFragment(pantryFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_pantry -> replaceFragment(pantryFragment)
                R.id.action_add_grocery -> replaceFragment(AddGroceryFragment())
                R.id.action_recipes -> replaceFragment(RecipesFragment())
                R.id.action_profile -> replaceFragment(profileFragment) // Add this case
                else -> false
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }

    // This method is now correct and will call the addGroceryItem method in PantryFragment
    fun addGroceryItemToPantry(groceryItem: GroceryItem) {
        pantryFragment.addGroceryItem(groceryItem)
    }
}
