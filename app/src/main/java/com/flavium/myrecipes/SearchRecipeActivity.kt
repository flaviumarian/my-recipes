package com.flavium.myrecipes

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flavium.myrecipes.constructors.Recipe
import com.google.firebase.database.*
import java.util.ArrayList

class SearchRecipeActivity : AppCompatActivity() {

    var listAdapter: RecipeAdapter? = null
    var recipesRecyclerView: RecyclerView? = null
    var dataSnapshots: ArrayList<DataSnapshot> = ArrayList()
    private lateinit var viewManager: RecyclerView.LayoutManager

    var recipes: ArrayList<Recipe> = ArrayList()
    var empty: Boolean = true

    companion object {
        var searchEditText: EditText? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_recipe)

        viewManager = LinearLayoutManager(applicationContext)


        initiateComponents()
    }

    private fun initiateComponents() {

        // Back button
        val backImageView = findViewById<ImageView>(R.id.backImageView)
        backImageView?.setOnClickListener {
            onBackPressed()
        }


        // Search edit text
        searchEditText = findViewById(R.id.searchEditText)
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun afterTextChanged(s: Editable) {
                if (s.toString() != "") {
                    filter(s.toString())
                } else {
                    filter("No text")
                }
            }
        })

        // Recipes
        getAllRecipes("Desserts")
        getAllRecipes("Bread")
        getAllRecipes("Soup")
        getAllRecipes("Sides")
        getAllRecipes("Pasta")
        getAllRecipes("Chicken")
        getAllRecipes("Fish")
        getAllRecipes("Pizza")
        getAllRecipes("Pork")
        getAllRecipes("Beef")
        getAllRecipes("Grill")
        getAllRecipes("Sauces")
        getAllRecipes("Salads")
        getAllRecipes("Appetizers")
        getAllRecipes("Pickles")
        getAllRecipes("Syrups")

        // RecyclerView
        recipesRecyclerView = findViewById(R.id.recipesRecyclerView)
        listAdapter = RecipeAdapter(
            applicationContext!!,
            ArrayList(),
            ArrayList()
        )

        recipesRecyclerView =
            findViewById<RecyclerView>(R.id.recipesRecyclerView).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = listAdapter
            }
        recipesRecyclerView?.isFocusable = false
    }

    private fun getAllRecipes(title: String){
        FirebaseDatabase.getInstance().getReference().child("users")
            .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
            .child(title)
            .addChildEventListener(object :
                ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    empty = false
                    dataSnapshots.add(snapshot)
                    recipes.add(
                        Recipe(
                            snapshot.child("title").value as String,
                            snapshot.child("description").value as String,
                            snapshot.child("ingredients").value as String,
                            snapshot.child("instructions").value as String,
                            snapshot.child("people").value as String,
                            snapshot.child("hours").value as String,
                            snapshot.child("minutes").value as String,
                            snapshot.child("image").value as String,
                            snapshot.child("imageName").value as String,
                            title
                        )
                    )


                    listAdapter?.notifyDataSetChanged()

                    findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                        View.GONE

                }

                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }
                override fun onChildRemoved(snapshot: DataSnapshot) {

                    var index = 0
                    var indexToRemove = -1

                    for (dataSnapshot: DataSnapshot in dataSnapshots) {

                        if (dataSnapshot.key == snapshot.key) {

                            indexToRemove = index
                            recipes.removeAt(index)

                            listAdapter?.notifyItemRemoved(index)
                        }

                        index++
                    }
//                    if (recipes.size == 0) {
//                        currentView?.findViewById<TextView>(R.id.infoTextView)?.visibility =
//                            View.VISIBLE
//                    }else{
//                        currentView?.findViewById<TextView>(R.id.infoTextView)?.visibility =
//                            View.INVISIBLE
//                    }

                    if (indexToRemove != -1) {
                        dataSnapshots.removeAt(indexToRemove)
                    }
                }
            })

        FirebaseDatabase.getInstance().getReference().child("users")
            .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
            .child(title)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (empty) {
                        findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                            View.GONE
//                        findViewById<TextView>(R.id.infoTextView)?.visibility =
//                            View.VISIBLE
                    }
                }

            })


    }

    private fun filter(text: String) {
        val filteredRecipes: ArrayList<Recipe> = ArrayList()
        val filteredSnapshots: ArrayList<DataSnapshot> = ArrayList()

        if (text != "No text") {
            for (recipe in recipes) {
                if (recipe?.title.toLowerCase()
                        .contains(text.toLowerCase()) || recipe.ingredients.toLowerCase()
                        .contains(text.toLowerCase()) || recipe.description.toLowerCase()
                        .contains(text.toLowerCase())
                ) {
                    filteredRecipes.add(recipe)
                    filteredSnapshots.add(
                        dataSnapshots.get(
                            recipes.indexOf(recipe)
                        )
                    )
                }
            }
        }

        listAdapter?.filterList(filteredRecipes, filteredSnapshots)
    }


    class RecipeAdapter(
        private val context: Context,
        private var recipes: ArrayList<Recipe>,
        private var dataSnapshots: ArrayList<DataSnapshot>
    ) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ViewHolder(
                inflater,
                parent
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val recipe: Recipe = recipes[position]
            holder.bind(recipe)


            holder.relativeLayout?.setOnClickListener {

                // Check internet connection
                if(HelpingFunctions.isOnline(context)){
                    val intent = Intent(context, SeeRecipeActivity::class.java)
                    intent.putExtra("recipe", recipe)
                    intent.putExtra("category", recipe.category)
                    intent.putExtra("key", dataSnapshots.get(position).key)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent)
                }else{
                    Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show()
                }


            }

        }

        override fun getItemCount(): Int = recipes.size

        fun filterList(
            filteredRecipes: ArrayList<Recipe>,
            filteredSnapshots: ArrayList<DataSnapshot>
        ) {
            recipes = filteredRecipes
            dataSnapshots = filteredSnapshots
            notifyDataSetChanged()
        }
    }

    class ViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.custom_recipe_list_card, parent, false
        )
    ) {

        var recipeTextView: TextView? = null
        var relativeLayout: RelativeLayout? = null

        init {
            recipeTextView = itemView.findViewById(R.id.recipeTextView)
            relativeLayout = itemView.findViewById(R.id.relativeLayout)
        }

        fun bind(recipe: Recipe) {
            recipeTextView?.text = recipe.title
        }
    }

    override fun onResume() {
        super.onResume()

        if(searchEditText != null){
            searchEditText?.setText("")
        }
    }
}
