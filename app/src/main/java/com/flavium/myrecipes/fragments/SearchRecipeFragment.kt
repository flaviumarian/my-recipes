package com.flavium.myrecipes.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flavium.myrecipes.HelpingFunctions
import com.flavium.myrecipes.R
import com.flavium.myrecipes.SeeRecipeActivity
import com.flavium.myrecipes.constructors.Recipe
import com.google.firebase.database.DataSnapshot
import java.util.*

class SearchRecipeFragment : Fragment() {

    var currentView: View? = null
    var searchEditText: EditText? = null
    var listAdapter: RecipeAdapter? = null
    var recipesRecyclerView: RecyclerView? = null
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.fragment_search_recipe, container, false)
        viewManager = LinearLayoutManager(view?.context)

        initiateComponents()


        return currentView
    }

    private fun initiateComponents() {

        // Back button
        val backImageView = currentView?.findViewById<ImageView>(R.id.backImageView)
        backImageView?.setOnClickListener {

            goBack()

        }

        // Search field
        searchEditText = currentView?.findViewById(R.id.searchEditText)
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


        // RecyclerView
        recipesRecyclerView = currentView?.findViewById(R.id.recipesRecyclerView)
        val recipes: ArrayList<Recipe> = ArrayList()
        val dataSnapshots: ArrayList<DataSnapshot> = ArrayList()

        listAdapter = RecipeAdapter(
            context!!,
            recipes,
            activity?.intent?.getStringExtra("title")!!,
            dataSnapshots
        )

        recipesRecyclerView =
            currentView!!.findViewById<RecyclerView>(R.id.recipesRecyclerView).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = listAdapter
            }
        recipesRecyclerView?.isFocusable = false

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

    private fun filter(text: String) {
        val filteredRecipes: ArrayList<Recipe> = ArrayList()
        val filteredSnapshots: ArrayList<DataSnapshot> = ArrayList()

        if (text != "No text") {
            for (recipe in RecipesDisplayFragment.recipesCopy) {
                if (recipe?.title.toLowerCase()
                        .contains(text.toLowerCase()) || recipe.ingredients.toLowerCase()
                        .contains(text.toLowerCase()) || recipe.description.toLowerCase()
                        .contains(text.toLowerCase())
                ) {
                    filteredRecipes.add(recipe)
                    filteredSnapshots.add(
                        RecipesDisplayFragment.dataSnapshotsCopy.get(
                            RecipesDisplayFragment.recipesCopy.indexOf(recipe)
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
        private val recipeCategory: String,
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
                    intent.putExtra("category", recipeCategory)
                    intent.putExtra("key", dataSnapshots.get(position).key)
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


}