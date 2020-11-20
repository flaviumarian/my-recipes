package com.flavium.myrecipes.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flavium.myrecipes.HelpingFunctions
import com.flavium.myrecipes.R
import com.flavium.myrecipes.RecipesActivity
import com.flavium.myrecipes.SeeRecipeActivity
import com.flavium.myrecipes.constructors.Recipe
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.firebase.database.*

class RecipesDisplayFragment : Fragment() {

    var currentView: View? = null
    var recipesRecyclerView: RecyclerView? = null
    var recipes: ArrayList<Recipe> = ArrayList()
    var dataSnapshots: ArrayList<DataSnapshot> = ArrayList()
    var empty: Boolean = true

    companion object {
        var recipesCopy: ArrayList<Recipe> = ArrayList()
        var dataSnapshotsCopy: ArrayList<DataSnapshot> = ArrayList()
    }

    var listAdapter: RecipeAdapter? = null
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentView = inflater.inflate(R.layout.fragment_recipe, container, false)

        // Ads
        val mAdView = currentView?.findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView?.loadAd(adRequest)

        viewManager = LinearLayoutManager(view?.context)
        getActivity()?.getApplicationContext()
        initiateComponents()
        setRecyclerView()



        FirebaseDatabase.getInstance().getReference().child("users")
            .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
            .child(activity?.intent?.getStringExtra("title")!!).addChildEventListener(object :
                ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    currentView?.findViewById<TextView>(R.id.infoTextView)?.visibility =
                        View.INVISIBLE
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
                            snapshot.child("imageName").value as String
                        )
                    )

                    recipesCopy = recipes
                    dataSnapshotsCopy = dataSnapshots

                    listAdapter?.notifyDataSetChanged()

                    currentView?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                        View.GONE


                }

                override fun onCancelled(error: DatabaseError) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                    var index = 0
                    var dataSnapshotIndex = -1

                    for (dataSnapshot: DataSnapshot in dataSnapshots) {
                        if (dataSnapshot.key == snapshot.key) {
                            dataSnapshotIndex = index

                            // Change recipe
                            val recipe = Recipe(
                                snapshot.child("title").value as String,
                                snapshot.child("description").value as String,
                                snapshot.child("ingredients").value as String,
                                snapshot.child("instructions").value as String,
                                snapshot.child("people").value as String,
                                snapshot.child("hours").value as String,
                                snapshot.child("minutes").value as String,
                                snapshot.child("image").value as String,
                                snapshot.child("imageName").value as String
                            )
                            recipes.set(index, recipe)

                            listAdapter?.notifyDataSetChanged()
                        }

                        index++
                    }

                    if (dataSnapshotIndex != -1) {
                        dataSnapshots.set(dataSnapshotIndex, snapshot)
                    }

                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                    var index = 0
                    var indexToRemove = -1

                    for (dataSnapshot: DataSnapshot in dataSnapshots) {
                        if (dataSnapshot.key == snapshot.key) {
                            indexToRemove = index
                            recipes.removeAt(index)

                            listAdapter?.notifyDataSetChanged()
                        }

                        index++
                    }
                    if (recipes.size == 0) {
                        currentView?.findViewById<TextView>(R.id.infoTextView)?.visibility =
                            View.VISIBLE
                    } else {
                        currentView?.findViewById<TextView>(R.id.infoTextView)?.visibility =
                            View.INVISIBLE
                    }

                    if (indexToRemove != -1) {
                        dataSnapshots.removeAt(indexToRemove)
                    }
                }
            })

        FirebaseDatabase.getInstance().getReference().child("users")
            .child(RecipesActivity.mAuth.currentUser!!.uid).child("recipes")
            .child(activity?.intent?.getStringExtra("title")!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (empty) {
                        currentView?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
                            View.GONE
                        currentView?.findViewById<TextView>(R.id.infoTextView)?.visibility =
                            View.VISIBLE
                    }
                }

            })



        return currentView
    }

    private fun initiateComponents() {
        // Back button
        val backImageView = currentView?.findViewById<ImageView>(R.id.backImageView)
        backImageView?.setOnClickListener {
            activity?.finish()
        }

        // Title text
        val categoryTitleTextView = currentView?.findViewById<TextView>(R.id.categoryTitleTextView)
        categoryTitleTextView?.text = activity?.intent?.getStringExtra("title")!!

        // Search image
        val searchImageView = currentView?.findViewById<ImageView>(R.id.searchImageView)
        searchImageView?.setOnClickListener {

            // Check internet connection
            if (HelpingFunctions.isOnline(currentView?.context!!)) {
                // Create new fragment and transaction
                val newFragment: Fragment =
                    SearchRecipeFragment()
                val transaction =
                    fragmentManager!!.beginTransaction()

                transaction.replace(R.id.fragmentContainer, newFragment)
                transaction.addToBackStack("search")

                transaction.commit()
            } else {
                Toast.makeText(currentView?.context!!, "No internet connection!", Toast.LENGTH_SHORT)
                    .show()
            }


        }

        // Add recipe
        val addRecipeCardView = currentView?.findViewById<CardView>(R.id.addRecipeCardView)
        addRecipeCardView?.setOnClickListener {

            // Check internet connection
            if (HelpingFunctions.isOnline(currentView?.context!!)) {
                // Create new fragment and transaction
                val newFragment: Fragment =
                    AddRecipeFragment()
                val transaction =
                    fragmentManager!!.beginTransaction()

                transaction.replace(R.id.fragmentContainer, newFragment)
                transaction.addToBackStack("add_recipe")

                transaction.commit()
            } else {
                Toast.makeText(currentView?.context!!, "No internet connection!", Toast.LENGTH_SHORT)
                    .show()
            }


        }
    }

    private fun setRecyclerView() {

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

    class RecipeAdapter(
        private val context: Context,
        private val recipes: ArrayList<Recipe>,
        private val recipeCategory: String,
        private val dataSnapshots: ArrayList<DataSnapshot>
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
                if (HelpingFunctions.isOnline(context)) {
                    val intent = Intent(context, SeeRecipeActivity::class.java)
                    intent.putExtra("recipe", recipe)
                    intent.putExtra("category", recipeCategory)
                    intent.putExtra("key", dataSnapshots.get(position).key)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "No internet connection!", Toast.LENGTH_SHORT).show()
                }
            }

        }

        override fun getItemCount(): Int = recipes.size

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