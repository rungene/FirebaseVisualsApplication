package com.rungenes.firebasevisualsapplication

import com.google.firebase.database.Exclude

class ModelClass {
    var title: String? = null
    var image: String? = null
    var description: String? = null

    @get:Exclude
    @set:Exclude
    var uid: String? = null
}