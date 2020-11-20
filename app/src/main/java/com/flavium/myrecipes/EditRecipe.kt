package com.flavium.myrecipes

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.flavium.myrecipes.constructors.Recipe
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_see_recipe.*
import java.io.ByteArrayOutputStream
import java.util.*


class EditRecipe : AppCompatActivity() {

    private var recipe: Recipe? = null

    var titleEditText: EditText? = null
    var descriptionEditText: EditText? = null
    var ingredientsEditText: EditText? = null
    var instructionsEditText: EditText? = null
    var numberOfPeopleSpinner: Spinner? = null
    var hoursSpinner: Spinner? = null
    var minutesSpinner: Spinner? = null
    var removePictureTextView: TextView? = null
    var addPictureImageView: ImageView? = null
    var addPicture: TextView? = null
    private val handler = Handler()


    var recipeImage: Bitmap? = null
    val imageName = UUID.randomUUID().toString() + ".jpg"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Ads
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        recipe = intent.getSerializableExtra("recipe") as? Recipe

        initiateComponents()

        deleteFunction()

        makeChanges()
    }

    private fun initiateComponents() {

        Thread(Runnable {

            // title
            titleEditText = findViewById(R.id.titleEditText)

            // description
            descriptionEditText = findViewById(R.id.descriptionEditText)

            // ingredients
            ingredientsEditText = findViewById(R.id.ingredientsEditText)

            // instructions
            instructionsEditText = findViewById(R.id.instructionsEditText)

            // number of people
            numberOfPeopleSpinner = findViewById(R.id.numberOfPeopleSpinner)
            val people = Array(21) { i -> i.toString() }

            // hours
            hoursSpinner = findViewById(R.id.hoursSpinner)
            val hours = Array(49) { i -> i.toString() }

            // minutes
            minutesSpinner = findViewById(R.id.minutesSpinner)
            val minutes = Array(60) { i -> i.toString() }

            // add image
            addPictureImageView = findViewById(R.id.addPictureImageView)

            // remove current image
            removePictureTextView = findViewById(R.id.removePictureTextView)

            handler.post(Runnable {

                // back
                findViewById<ImageView>(R.id.backImageView).setOnClickListener {
                    onBackPressed()
                }

                // title
                titleEditText?.setText(recipe?.title)

                // description
                descriptionEditText?.setText(recipe?.description)

                // ingredients
                ingredientsEditText?.setText(recipe?.ingredients)

                // instructions
                instructionsEditText?.setText(recipe?.instructions)

                // number of people
                if (numberOfPeopleSpinner != null) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, people)
                    numberOfPeopleSpinner?.adapter = adapter
                    numberOfPeopleSpinner?.setSelection(Integer.parseInt(recipe?.nrPeople!!))
                }

                // hours
                if (hoursSpinner != null) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, hours)
                    hoursSpinner?.adapter = adapter
                    hoursSpinner?.setSelection(Integer.parseInt(recipe?.hours!!))
                }

                // minutes
                if (minutesSpinner != null) {
                    val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, minutes)
                    minutesSpinner?.adapter = adapter
                    minutesSpinner?.setSelection(Integer.parseInt(recipe?.minutes!!))
                }

                // image text
                addPicture = findViewById(R.id.addPicture)

                // image
                if(SeeRecipeActivity.currentImageBitmap != null){
                    addPicture?.text = resources.getText(R.string.change_picture)
                    addPictureImageView?.setImageBitmap(SeeRecipeActivity.currentImageBitmap)
                }

                addPictureImageView?.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 1)
                }

                val addPicture = findViewById<TextView>(R.id.addPicture)
                addPicture?.setOnClickListener {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, 1)
                }

                // remove current image
                if (!recipe?.imageURL.equals("")) {
                    removePictureTextView?.visibility = View.VISIBLE
                }
                removePictureTextView?.setOnClickListener {
                    removePictureTextView?.visibility = View.GONE
                    recipeImage = null
                    addPictureImageView?.setImageResource(R.drawable.ic_baseline_image_24)
                    addPicture?.text = resources.getText(R.string.add_picture_regular)
                }

                // stop progress bar
                findViewById<RelativeLayout>(R.id.loadingPanel)?.visibility = View.GONE
            })
        }).start()

    }

    private fun deleteFunction() {
        findViewById<CardView>(R.id.removeRecipeCard).setOnClickListener {

            // Check internet connection
            if(HelpingFunctions.isOnline(this)){
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.custom_dialog_delete_recipe)

                val closeImageView = dialog.findViewById(R.id.closeImageView) as ImageView
                closeImageView.setOnClickListener {
                    dialog.dismiss()
                }
                val yesBtn = dialog.findViewById(R.id.yesButton) as Button
                val noBtn = dialog.findViewById(R.id.noButton) as TextView
                yesBtn.setOnClickListener {
                    dialog.dismiss()
                    FirebaseDatabase.getInstance().getReference().child("users")
                        .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
                        .child(intent.getStringExtra("category")!!)
                        .child(intent.getStringExtra("key")!!).removeValue()

                    if (!recipe?.imageURL.equals("")) {

                        FirebaseStorage.getInstance().getReference().child("images")
                            .child(recipe?.imageName!!)
                            .delete()

                    }


                    setResult(2)
                    onBackPressed()
                }
                noBtn.setOnClickListener { dialog.dismiss() }
                dialog.show()

            }else{
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun makeChanges() {

        findViewById<ImageView>(R.id.confirmChangesImageView).setOnClickListener {

            // Check internet connection
            if(HelpingFunctions.isOnline(this)){
                if (titleEditText?.text.isNullOrBlank()) {
                    titleEditText?.setError("Insert title")

                } else {

                    if (recipeImage != null) {
                        // an image has been added


                        // Delete old picture if it exists
                        if (!recipe?.imageURL.equals("")) {
                            FirebaseStorage.getInstance().getReference().child("images")
                                .child(recipe?.imageName!!)
                                .delete()
                        }

                        // Upload new picture and update fields
                        val baos = ByteArrayOutputStream()
                        recipeImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val uploadTask =
                            FirebaseStorage.getInstance().getReference().child("images")
                                .child(imageName)
                                .putBytes(data)
                        uploadTask.addOnFailureListener {
                            // Handle unsuccessful uploads
                            Toast.makeText(this, "Upload Failed.", Toast.LENGTH_SHORT).show()
                        }.addOnSuccessListener { taskSnapshot ->
                            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->

                                val recipeMap: Map<String, String> = mapOf(
                                    "title" to titleEditText?.text.toString(),
                                    "description" to descriptionEditText?.text.toString(),
                                    "ingredients" to ingredientsEditText?.text.toString(),
                                    "instructions" to instructionsEditText?.text.toString(),
                                    "people" to numberOfPeopleSpinner?.selectedItem.toString(),
                                    "hours" to hoursSpinner?.selectedItem.toString(),
                                    "minutes" to minutesSpinner?.selectedItem.toString(),
                                    "image" to uri.toString(),
                                    "imageName" to imageName
                                )

                                FirebaseDatabase.getInstance().getReference().child("users")
                                    .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
                                    .child(intent.getStringExtra("category")!!)
                                    .child(intent.getStringExtra("key")!!).setValue(recipeMap)

                                // Create recipe
                                val recipe = Recipe(
                                    recipeMap.get("title")!!,
                                    recipeMap.get("description")!!,
                                    recipeMap.get("ingredients")!!,
                                    recipeMap.get("instructions")!!,
                                    recipeMap.get("people")!!,
                                    recipeMap.get("hours")!!,
                                    recipeMap.get("minutes")!!,
                                    recipeMap.get("image")!!,
                                    recipeMap.get("imageName")!!
                                )


                                // Send recipe
                                val resultIntent = Intent()
                                resultIntent.putExtra("recipe", recipe)
                                setResult(Activity.RESULT_OK, resultIntent)
                                onBackPressed()

                            }
                        }
                    } else if (removePictureTextView?.visibility == View.GONE && !recipe?.imageURL.equals(
                            ""
                        )
                    ) {
                        // the old image has been removed with no new picture added

                        // Delete old picture
                        FirebaseStorage.getInstance().getReference().child("images")
                            .child(recipe?.imageName!!)
                            .delete()

                        // Update the rest of the fields
                        val recipeMap: Map<String, String> = mapOf(
                            "title" to titleEditText?.text.toString(),
                            "description" to descriptionEditText?.text.toString(),
                            "ingredients" to ingredientsEditText?.text.toString(),
                            "instructions" to instructionsEditText?.text.toString(),
                            "people" to numberOfPeopleSpinner?.selectedItem.toString(),
                            "hours" to hoursSpinner?.selectedItem.toString(),
                            "minutes" to minutesSpinner?.selectedItem.toString(),
                            "image" to "",
                            "imageName" to ""
                        )

                        FirebaseDatabase.getInstance().getReference().child("users")
                            .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
                            .child(intent.getStringExtra("category")!!)
                            .child(intent.getStringExtra("key")!!).setValue(recipeMap)


                        // Create recipe
                        val recipe = Recipe(
                            recipeMap.get("title")!!,
                            recipeMap.get("description")!!,
                            recipeMap.get("ingredients")!!,
                            recipeMap.get("instructions")!!,
                            recipeMap.get("people")!!,
                            recipeMap.get("hours")!!,
                            recipeMap.get("minutes")!!,
                            recipeMap.get("image")!!,
                            recipeMap.get("imageName")!!
                        )


                        // Send recipe
                        val resultIntent = Intent()
                        resultIntent.putExtra("recipe", recipe)
                        setResult(Activity.RESULT_OK, resultIntent)
                        onBackPressed()


                    } else {
                        // No changes to the image, we keep recipe.imageURL and recipe.imageName
                        val recipeMap: Map<String, String> = mapOf(
                            "title" to titleEditText?.text.toString(),
                            "description" to descriptionEditText?.text.toString(),
                            "ingredients" to ingredientsEditText?.text.toString(),
                            "instructions" to instructionsEditText?.text.toString(),
                            "people" to numberOfPeopleSpinner?.selectedItem.toString(),
                            "hours" to hoursSpinner?.selectedItem.toString(),
                            "minutes" to minutesSpinner?.selectedItem.toString(),
                            "image" to recipe?.imageURL!!,
                            "imageName" to recipe?.imageName!!
                        )

                        FirebaseDatabase.getInstance().getReference().child("users")
                            .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
                            .child(intent.getStringExtra("category")!!)
                            .child(intent.getStringExtra("key")!!).setValue(recipeMap)

                        // Create recipe
                        val recipe = Recipe(
                            recipeMap.get("title")!!,
                            recipeMap.get("description")!!,
                            recipeMap.get("ingredients")!!,
                            recipeMap.get("instructions")!!,
                            recipeMap.get("people")!!,
                            recipeMap.get("hours")!!,
                            recipeMap.get("minutes")!!,
                            recipeMap.get("image")!!,
                            recipeMap.get("imageName")!!
                        )


                        // Send recipe
                        val resultIntent = Intent()
                        resultIntent.putExtra("recipe", recipe)
                        setResult(Activity.RESULT_OK, resultIntent)
                        onBackPressed()
                    }
                }

            }else{
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show()
            }




        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val selectedImage = data!!.data
                recipeImage = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                removePictureTextView?.visibility = View.VISIBLE
                addPictureImageView?.setImageBitmap(recipeImage)
                addPicture?.text = resources.getText(R.string.change_picture)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()


        // Hide keyboard
        try {
            val imm =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

