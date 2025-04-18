package com.example.pantrypal

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var pantryFragment: PantryFragment
    lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    fun addGroceryItemToPantry(groceryItem: GroceryItem) {
        pantryFragment.addGroceryItem(groceryItem)
    }
}

