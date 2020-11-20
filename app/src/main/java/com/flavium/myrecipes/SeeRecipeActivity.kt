package com.flavium.myrecipes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flavium.myrecipes.constructors.Recipe
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SeeRecipeActivity : AppCompatActivity() {

    private var recipe: Recipe? = null
    private var category: String? = null
    private var key: String? = null
    private var imageBitmap: Bitmap? = null
    private val handler = Handler()

    companion object{
        var currentImageBitmap: Bitmap? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_see_recipe)

        // Ads
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        recipe = intent.getSerializableExtra("recipe") as? Recipe
        category = intent.getStringExtra("category")
        key = intent.getStringExtra("key")



        initiateComponents()

    }

    private fun initiateComponents(){

        // title
        findViewById<TextView>(R.id.titleTextView)?.text = recipe?.title

        // time
        if(Integer.parseInt(recipe!!.minutes) != 0 || Integer.parseInt(recipe!!.hours) != 0){
            findViewById<ImageView>(R.id.timeImageView)?.visibility = View.VISIBLE

            // if we have hours
            if(Integer.parseInt(recipe!!.hours) != 0){
                findViewById<TextView>(R.id.hoursTextView)?.text = recipe!!.hours.plus("h")
                findViewById<TextView>(R.id.hoursTextView)?.visibility = View.VISIBLE
            }else{
                findViewById<TextView>(R.id.hoursTextView)?.visibility = View.GONE
            }

            // if we have minutes
            if(Integer.parseInt(recipe!!.minutes) != 0){
                findViewById<TextView>(R.id.minutesTextView)?.text = recipe!!.minutes.plus("m")
                findViewById<TextView>(R.id.minutesTextView)?.visibility = View.VISIBLE
            }else{
                findViewById<TextView>(R.id.minutesTextView)?.visibility = View.GONE
            }

        }else{
            findViewById<ImageView>(R.id.timeImageView)?.visibility = View.GONE
            findViewById<TextView>(R.id.hoursTextView)?.visibility = View.GONE
            findViewById<TextView>(R.id.minutesTextView)?.visibility = View.GONE
        }

        // people
        if(Integer.parseInt(recipe!!.nrPeople) != 0){
            findViewById<ImageView>(R.id.peopleImageView)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.peopleTextView)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.peopleTextView)?.text = recipe!!.nrPeople.plus(" people")
        }else{
            findViewById<ImageView>(R.id.peopleImageView)?.visibility = View.GONE
            findViewById<TextView>(R.id.peopleTextView)?.visibility = View.GONE
        }

        // view
        if(!recipe!!.description.equals("") || !recipe!!.ingredients.equals("") || !recipe!!.instructions.equals("")){
            findViewById<View>(R.id.view)?.visibility = View.VISIBLE
        }else{
            findViewById<View>(R.id.view)?.visibility = View.GONE
        }

        // description
        if(!recipe!!.description.equals("")){
            findViewById<TextView>(R.id.descriptionTextView)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.descriptionTextView)?.text = recipe!!.description
        }else{
            findViewById<TextView>(R.id.descriptionTextView)?.visibility = View.GONE
        }

        // ingredients
        if(!recipe!!.ingredients.equals("")){
            findViewById<TextView>(R.id.ingredients)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.ingredientsTextView)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.ingredientsTextView)?.text = recipe!!.ingredients
        }else{
            findViewById<TextView>(R.id.ingredients)?.visibility = View.GONE
            findViewById<TextView>(R.id.ingredientsTextView)?.visibility = View.GONE
        }

        // instructions
        if(!recipe!!.instructions.equals("")){
            findViewById<TextView>(R.id.instructions)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.instructionsTextView)?.visibility = View.VISIBLE
            findViewById<TextView>(R.id.instructionsTextView)?.text = recipe!!.instructions
        }else{
            findViewById<TextView>(R.id.instructions)?.visibility = View.GONE
            findViewById<TextView>(R.id.instructionsTextView)?.visibility = View.GONE
        }

        // image
        if(!recipe!!.imageURL.equals("")){

            Thread(Runnable {
                // Download image
                val task = ImageDownloader()

                try{
                    imageBitmap = task.execute(recipe!!.imageURL).get()
                    currentImageBitmap = imageBitmap

                    handler.post(Runnable {
                        findViewById<ImageView>(R.id.recipeImageView)?.setImageBitmap(imageBitmap)
                    })
                } catch(e: Exception){
                    e.printStackTrace()
                }
            }).start()

        }else{
            currentImageBitmap = null
            findViewById<ImageView>(R.id.recipeImageView)?.setImageResource(0)
        }

        // back
        findViewById<ImageView>(R.id.backImageView).setOnClickListener{
            onBackPressed()
        }

        // edit
        findViewById<ImageView>(R.id.addRecipeImageView).setOnClickListener {

            // Check internet connection
            if(HelpingFunctions.isOnline(this)){
                val intent = Intent(this, EditRecipe::class.java)
                intent.putExtra("recipe", recipe)
                intent.putExtra("category", category)
                intent.putExtra("key", key)

                startActivityForResult(intent, 0)
            }else{
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    inner class ImageDownloader : AsyncTask<String, Void, Bitmap>(){

        override fun doInBackground(vararg urls: String?): Bitmap? {

            try{
                val url = URL(urls[0])
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()

                val `in` = connection.inputStream
                return BitmapFactory.decodeStream(`in`)
            }catch(e: Exception){
                e.printStackTrace()
                return null
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == 2){
            finish()
        }

        if(requestCode == 0 && resultCode== Activity.RESULT_OK){

            recipe = data?.getSerializableExtra("recipe") as? Recipe
            if(recipe?.imageURL.equals("")){
                imageBitmap = null
            }
            initiateComponents()
        }
    }
}
