package com.flavium.myrecipes.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.flavium.myrecipes.HelpingFunctions
import com.flavium.myrecipes.R
import com.flavium.myrecipes.RecipesActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.*

class AddRecipeFragment : Fragment() {

    var currentView: View? = null
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

    var recipeImage: Bitmap? = null
    val imageName = UUID.randomUUID().toString() + ".jpg"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.fragment_add_recipe, container, false)

        // Ads
        val mAdView = currentView?.findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        initiateComponents()

        return currentView
    }

    private fun initiateComponents() {

        // Back button
        val backImageView = currentView?.findViewById<ImageView>(R.id.backImageView)
        backImageView?.setOnClickListener {

            goBack()

        }

        // Add recipe
        val addRecipeImageView = currentView?.findViewById<ImageView>(R.id.addRecipeImageView)
        addRecipeImageView?.setOnClickListener {

            addRecipe()

        }

        // Instantiate components
        titleEditText = currentView?.findViewById(R.id.titleEditText)
        descriptionEditText = currentView?.findViewById(R.id.descriptionEditText)
        ingredientsEditText = currentView?.findViewById(R.id.ingredientsEditText)
        instructionsEditText = currentView?.findViewById(R.id.instructionsEditText)
        numberOfPeopleSpinner = currentView?.findViewById(R.id.numberOfPeopleSpinner)
        hoursSpinner = currentView?.findViewById(R.id.hoursSpinner)
        minutesSpinner = currentView?.findViewById(R.id.minutesSpinner)


        // numberOfPeople
        val people = Array(21) { i -> i.toString() }
        if (numberOfPeopleSpinner != null) {
            val adapter =
                ArrayAdapter(currentView!!.context, android.R.layout.simple_spinner_item, people)
            numberOfPeopleSpinner?.adapter = adapter
        }

        // hours
        val hours = Array(49) { i -> i.toString() }
        if (hoursSpinner != null) {
            val adapter =
                ArrayAdapter(currentView!!.context, android.R.layout.simple_spinner_item, hours)
            hoursSpinner?.adapter = adapter
        }

        // minutes
        val minutes = Array(60) { i -> i.toString() }
        if (minutesSpinner != null) {
            val adapter =
                ArrayAdapter(currentView!!.context, android.R.layout.simple_spinner_item, minutes)
            minutesSpinner?.adapter = adapter
        }

        // add image
        addPictureImageView = currentView?.findViewById(R.id.addPictureImageView)
        addPictureImageView?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }

        addPicture = currentView?.findViewById(R.id.addPicture)

        val addPicture = currentView?.findViewById<TextView>(R.id.addPicture)
        addPicture?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1)
        }


        // remove current image
        removePictureTextView = currentView?.findViewById(R.id.removePictureTextView)
        removePictureTextView?.setOnClickListener {
            removePictureTextView?.visibility = View.GONE
            recipeImage = null
            addPictureImageView?.setImageResource(R.drawable.ic_baseline_image_24)
            addPicture?.text = resources.getText(R.string.add_picture_regular)
        }
    }

    private fun addRecipe() {

        // Check internet connection
        if(!HelpingFunctions.isOnline(currentView?.context!!)){
            Toast.makeText(currentView?.context!!, "No internet connection!", Toast.LENGTH_SHORT).show()
            return
        }

        if (titleEditText?.text.isNullOrBlank()) {
            titleEditText?.setError("Insert title")
            return
        }


        // check all fields
        if (recipeImage == null) {
            // Without image

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
                .child(activity?.intent?.getStringExtra("title")!!).push().setValue(recipeMap)

        } else {
            // With image
            val baos = ByteArrayOutputStream()
            recipeImage?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val uploadTask =
                FirebaseStorage.getInstance().getReference().child("images").child(imageName)
                    .putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
                Toast.makeText(currentView?.context, "Upload Failed.", Toast.LENGTH_SHORT).show()
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
                        .child(activity?.intent?.getStringExtra("title")!!).push()
                        .setValue(recipeMap)


                }
            }
        }

        goBack()

    }

    private fun goBack() {
        // Hide keyboard
        try {
            val imm =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Create new fragment and transaction
        val newFragment: Fragment = RecipesDisplayFragment()
        val transaction =
            fragmentManager!!.beginTransaction()

        transaction.replace(R.id.fragmentContainer, newFragment)
        transaction.addToBackStack("landing_page")

        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val selectedImage = data!!.data
                recipeImage = MediaStore.Images.Media.getBitmap(
                    currentView?.context?.contentResolver,
                    selectedImage
                )
                addPictureImageView?.setImageBitmap(recipeImage)
                removePictureTextView?.visibility = View.VISIBLE
                addPicture?.text = resources.getText(R.string.change_picture)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}