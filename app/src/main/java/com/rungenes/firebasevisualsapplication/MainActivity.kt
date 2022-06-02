package com.rungenes.firebasevisualsapplication

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.rungenes.firebasevisualsapplication.ModelClass
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.storage.FirebaseStorage
import android.os.Bundle
import com.rungenes.firebasevisualsapplication.R
import com.google.firebase.auth.FirebaseUser
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.rungenes.firebasevisualsapplication.MainActivity
import android.content.Intent
import android.app.Activity
import android.widget.Toast
import android.content.DialogInterface
import android.view.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import androidx.appcompat.widget.SearchView
import com.google.firebase.database.*
import com.rungenes.firebasevisualsapplication.ViewHolder.ClickListener
import com.rungenes.firebasevisualsapplication.ImageDetailsActivity
import com.rungenes.firebasevisualsapplication.ImageAddActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private var mfirebaseDatabase: FirebaseDatabase? = null
    private var mRef: DatabaseReference? = null

    // private Button btnLogout;
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    private var firebaseStorage: FirebaseStorage? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        mAuth = FirebaseAuth.getInstance()

        val actionBar = supportActionBar
        actionBar!!.setTitle("MDS List")

        //sending the query to the Firebase
        mfirebaseDatabase = FirebaseDatabase.getInstance()
        mRef = mfirebaseDatabase!!.getReference("Data")
        firebaseStorage = FirebaseStorage.getInstance()

        mRef!!.keepSynced(true)
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //signed in user
                onInitializeSignedin(user.displayName)


                //      startActivity(new Intent(MainActivity.this,LoginActivity.class));
            } else {
                //user is signed out
                onSignedOutCleanup()


startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder() //.setEmailLink(link)
                        .setAvailableProviders(
                            Arrays.asList(
                                GoogleBuilder().build(),
                                EmailBuilder().build()
                            )
                        ).build(),
                    RC_SIGN_IN
                )
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show()
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mAuth!!.removeAuthStateListener(mAuthListener!!)
    }

    override fun onResume() {
        super.onResume()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    private fun onInitializeSignedin(displayName: String?) {}
    private fun onSignedOutCleanup() {}



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //inflating the menu ,this adds items to the action bar if presenent
        menuInflater.inflate(R.menu.menu, menu)


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        //handle other action bar item clicks hear
        if (id == R.id.menu_logout) {
            //signout
            AuthUI.getInstance().signOut(this)
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        // Choose an arbitrary request code value
        const val RC_SIGN_IN = 123
    }
}