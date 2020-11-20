package com.flavium.myrecipes.constructors

import java.io.Serializable

class Recipe constructor(val title: String, val description: String, val ingredients: String, val instructions: String, val nrPeople: String, val hours: String, val minutes: String, val imageURL: String, val imageName: String, val category: String ? = null) : Serializable