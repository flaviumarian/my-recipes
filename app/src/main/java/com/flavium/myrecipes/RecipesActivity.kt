package com.flavium.myrecipes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flavium.myrecipes.fragments.RecipesDisplayFragment
import com.flavium.myrecipes.fragments.SearchRecipeFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class RecipesActivity : AppCompatActivity() {

    // Used in fragment
    companion object{
        val title : String? = null
        var mAuth : FirebaseAuth = Firebase.auth
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipes)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer,
            RecipesDisplayFragment()
        )
        transaction.addToBackStack("landing_page")
        transaction.commit()

    }

    override fun onResume() {
        super.onResume()

        title = intent.getStringExtra("title")

        val fragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        if (fragment is SearchRecipeFragment) {
            val transaction =
                supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer,
                RecipesDisplayFragment()
            )
            transaction.addToBackStack("landing_page")
            transaction.commit()
        }

    }

    override fun onBackPressed() {

        val fragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (fragment is RecipesDisplayFragment) {
            finish()
        } else {

            val transaction =
                supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer,
                RecipesDisplayFragment()
            )
            transaction.addToBackStack("landing_page")
            transaction.commit()
        }
    }
}
