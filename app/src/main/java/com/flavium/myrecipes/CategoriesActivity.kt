package com.flavium.myrecipes

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flavium.myrecipes.constructors.Category
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class CategoriesActivity : AppCompatActivity() {

    var mAuth: FirebaseAuth = Firebase.auth
    var categories: ArrayList<Category> = ArrayList()
    var categoriesRecyclerView: RecyclerView? = null
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var context: Context


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        // Ads
        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        if (resources.getBoolean(R.bool.isTablet)) {
            viewManager = GridLayoutManager(this, 3)
        } else {
            viewManager = GridLayoutManager(this, 2)
        }


        context = this

        generateCategories()

        setRecyclerView()

        // Search image
        findViewById<ImageView>(R.id.searchImageView).setOnClickListener {

            // Check internet connection
            if (HelpingFunctions.isOnline(this)) {
                val intent = Intent(this, SearchRecipeActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }


    private fun generateCategories() {

        categories.add(
            Category(
                "Desserts",
                R.drawable.ic_desserts
            )
        )
        categories.add(
            Category(
                "Bread",
                R.drawable.ic_bread
            )
        )
        categories.add(
            Category(
                "Soup",
                R.drawable.ic_soup
            )
        )
        categories.add(
            Category(
                "Sides",
                R.drawable.ic_sides
            )
        )
        categories.add(
            Category(
                "Pasta",
                R.drawable.ic_pasta
            )
        )

        categories.add(
            Category(
                "Chicken",
                R.drawable.ic_chicken
            )
        )
        categories.add(
            Category(
                "Fish",
                R.drawable.ic_fish
            )
        )
        categories.add(
            Category(
                "Pizza",
                R.drawable.ic_pizza
            )
        )
        categories.add(
            Category(
                "Pork",
                R.drawable.ic_pork
            )
        )
        categories.add(
            Category(
                "Beef",
                R.drawable.ic_beef
            )
        )
        categories.add(
            Category(
                "Grill",
                R.drawable.ic_grill
            )
        )
        categories.add(
            Category(
                "Sauces",
                R.drawable.ic_sauces
            )
        )
        categories.add(
            Category(
                "Salads",
                R.drawable.ic_salads
            )
        )
        categories.add(
            Category(
                "Appetizers",
                R.drawable.ic_appetizers
            )
        )
        categories.add(
            Category(
                "Pickles",
                R.drawable.ic_pickles
            )
        )

        categories.add(
            Category(
                "Syrups",
                R.drawable.ic_drinks
            )
        )

    }

    private fun setRecyclerView() {

        categoriesRecyclerView = findViewById<RecyclerView>(R.id.categoriesRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = CategoryAdapter(context, categories)
        }
        categoriesRecyclerView?.isFocusable = false
    }


    class CategoryAdapter(
        private val context: Context,
        private val categories: ArrayList<Category>
    ) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ViewHolder(inflater, parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val category: Category = categories[position]
            holder.bind(category)

            holder.relativeLayout?.setOnClickListener {

                // Check internet connection
                if (HelpingFunctions.isOnline(context)) {
                    var intent = Intent(context, RecipesActivity::class.java)
                    intent.putExtra("title", category.title)
                    startActivity(context, intent, null)
                } else {
                    Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show()
                }
            }

        }


        override fun getItemCount(): Int = categories.size

    }

    class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.custom_category_card, parent, false)) {

        var categoryTextView: TextView? = null
        var categoryImageView: ImageView? = null
        var relativeLayout: RelativeLayout? = null

        init {
            categoryTextView = itemView.findViewById(R.id.categoryTextView)
            categoryImageView = itemView.findViewById(R.id.categoryImageView)
            relativeLayout = itemView.findViewById(R.id.relativeLayout)
        }

        fun bind(category: Category) {
            categoryTextView?.text = category.title
            categoryImageView?.setImageResource(category.imageName)
        }
    }


    override fun onBackPressed() {

        // Check internet connection
        if(!HelpingFunctions.isOnline(this)){
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show()
            return
        }


        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.custom_dialog_delete_recipe)

        val closeImageView = dialog.findViewById(R.id.closeImageView) as ImageView
        closeImageView.setOnClickListener {
            dialog.dismiss()
        }

        val text = dialog.findViewById(R.id.text) as TextView
        text?.text = getString(R.string.log_out_message)

        val yesBtn = dialog.findViewById(R.id.yesButton) as Button
        val noBtn = dialog.findViewById(R.id.noButton) as TextView
        yesBtn.setOnClickListener {
            dialog.dismiss()
            mAuth.signOut()
            super.onBackPressed()
        }
        noBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()


    }
}
