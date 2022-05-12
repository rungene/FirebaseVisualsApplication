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
    private var mRecyclerview: RecyclerView? = null
    private var mfirebaseDatabase: FirebaseDatabase? = null
    private var mRef: DatabaseReference? = null
    private var mLayoutManager //sorting
            : LinearLayoutManager? = null
    private var mSharedPreferences //saving sorting settings
            : SharedPreferences? = null

    // private Button btnLogout;
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    //Folder path to Firebase storage.
    private val uploadStoragePath = "All_Image_Upload/"
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<ModelClass, ViewHolder>? = null
    private var options: FirebaseRecyclerOptions<ModelClass>? = null
    private var firebaseStorage: FirebaseStorage? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FacebookSdk.sdkInitialize(getApplicationContext());
        mAuth = FirebaseAuth.getInstance()
        mRecyclerview = findViewById(R.id.recyclerView)
        val actionBar = supportActionBar
        actionBar!!.setTitle("MDS List")

        //since the default value is the newest so for the first time it will display newest post first.
        mSharedPreferences = getSharedPreferences("SortingSettings", MODE_PRIVATE)
        val mSorting = mSharedPreferences?.getString("Sort", "newest") //where no setting is selected



        //newest becomes the default
        if (mSorting == "newest") {
            mLayoutManager = LinearLayoutManager(this)

            //this will load the items from the bottom means newest  first.
            mLayoutManager!!.reverseLayout = true
            mLayoutManager!!.stackFromEnd = true
        } else if (mSorting == "oldest") {
            mLayoutManager = LinearLayoutManager(this)

            //this will load the items from the bottom means oldest  first.
            mLayoutManager!!.reverseLayout = false
            mLayoutManager!!.stackFromEnd = false
        }
        mRecyclerview?.setHasFixedSize(true)


        //sending the query to the Firebase
        mfirebaseDatabase = FirebaseDatabase.getInstance()
        mRef = mfirebaseDatabase!!.getReference("Data")
        firebaseStorage = FirebaseStorage.getInstance()
        showData()
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
    private fun showDeleteDataDialog(
        currentTitle: String?,
        currentImageUrl: String?,
        currentImageId: String?
    ) {

        //alert dialog
        val alertDialog = AlertDialog.Builder(this@MainActivity)
        alertDialog.setTitle("Delete")
        alertDialog.setMessage("Are you sure you want to delete this post?")
        //set positive/yes button
        alertDialog.setPositiveButton("Yes") { dialog, which ->
            //user pressed "Yes",delete data from firebase.
            /*whenever we publish a post the parent key is automatically created
                    since we do not know the key for the items to remove we will first need to query the DB to
                    * determine those keys.
                    * */

            //getting image upload id
            //String imageUploadId = mRef.push().getKey();


            //Query query = mRef.orderByChild("title").equalTo(currentImageId);
            val query1 = mRef!!.orderByKey().equalTo(currentImageId)
            Toast.makeText(this@MainActivity, "id$currentImageId", Toast.LENGTH_SHORT).show()
            query1.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {

                        // String key = dataSnapshot.getKey();
                        ds.ref.removeValue() //remove values from firebase when title matches.
                    }
                    //show a toast that the post was removed successfully
                    Toast.makeText(
                        this@MainActivity,
                        "Post Deleted Successfully..",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //if anything goes wrong show error message.
                    Toast.makeText(this@MainActivity, databaseError.message, Toast.LENGTH_SHORT)
                        .show()
                }
            })

            // delete the image using a url reference from firebase storage
            if (currentTitle != null && currentImageUrl != null && uploadStoragePath.contains(
                    currentImageUrl
                )
            ) {
                val storageReferencePicture =
                    FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl)
                storageReferencePicture.delete().addOnCompleteListener { //deleted successfully
                    Toast.makeText(
                        this@MainActivity,
                        "Image deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e -> //failure to delete
                    //something went wrong.
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        alertDialog.setNegativeButton("No") { dialog, which -> //user pressed "No", just dismiss the dialog
            dialog.dismiss()
        }

        //show dialog
        alertDialog.create().show()
    }

    //show data
    private fun showData() {
        options =
            FirebaseRecyclerOptions.Builder<ModelClass>()
                .setQuery(mRef!!, ModelClass::class.java)
                .build()

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ModelClass, ViewHolder>(
            options as FirebaseRecyclerOptions<ModelClass>
        ) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ModelClass) {
                holder.setDetails(
                    applicationContext,
                    model.title,
                    model.image,
                    model.description
                )
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {

                //inflating layout row.xml
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.rowitem, viewGroup, false)
                val viewHolder = ViewHolder(view)

                //item click listener
                viewHolder.setOnClickListener(object : ClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        //gettting data from firebase from the position clicked.
                        val mTitle = getItem(position).title
                        val mDesc = getItem(position).description
                        val mImage = getItem(position).image
                        // String postId = getItem(position).getUid();


                        //passing data to the new activity
                        val intent = Intent(view?.context, ImageDetailsActivity::class.java)
                        intent.putExtra("title", mTitle) //put title
                        intent.putExtra("description", mDesc) //put description
                        intent.putExtra("image", mImage) //put image url
                        // intent.putExtra("uid",postId);//put post id
                        startActivity(intent)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {

                        //get the current title
                        val cTitle = getItem(position).title


                        //get the current description
                        val cDesc = getItem(position).description

                        //get the current image url
                        val cImageUrl = getItem(position).image
                        //get the current postId
                        val cImageId = getRef(position).key

                        //show dialog on long click
                        val builder = AlertDialog.Builder(this@MainActivity)

                        //options to display in a dialog
                        val options = arrayOf("Update", "Delete")

                        //setting dialog
                        builder.setItems(options) { dialog, which ->
                            //handle dialogs item clicks
                            if (which == 0) {

                                //update clicked
                                //start activity with putting the current data
                                val intent = Intent(this@MainActivity, ImageAddActivity::class.java)
                                intent.putExtra("cTitle", cTitle)
                                intent.putExtra("cDesc", cDesc)
                                intent.putExtra("cImageUrl", cImageUrl)
                                intent.putExtra("cImageId", cImageId)
                                startActivity(intent)
                            }
                            if (which == 1) {

                                //delete click
                                //method call
                                showDeleteDataDialog(cTitle, cImageUrl, cImageId)
                            }
                        }
                        builder.create().show() //show dialog
                    }
                })
                return viewHolder
            }
        }


        //set layout as a linear layout
        mRecyclerview!!.layoutManager = mLayoutManager
        (firebaseRecyclerAdapter as FirebaseRecyclerAdapter<ModelClass, ViewHolder>).startListening()

        //set adapter to firebase recyclerview
        mRecyclerview!!.adapter = firebaseRecyclerAdapter
    }

    //search the data
    private fun firebaseSearch(textSearch: String) {
        //convert string entered in serchview to lower case
        val query = textSearch.toLowerCase()
        val firebaseQuery = mRef!!.orderByChild("search").startAt(query).endAt(query + "\uf8ff")
        options = FirebaseRecyclerOptions.Builder<ModelClass>()
            .setQuery(firebaseQuery, ModelClass::class.java).build()
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<ModelClass, ViewHolder>(
            options!!
        ) {
            override fun onBindViewHolder(holder: ViewHolder, position: Int, model: ModelClass) {
                holder.setDetails(
                    applicationContext,
                    model.title,
                    model.image,
                    model.description
                )
            }

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {

                //inflating layout row.xml
                val view = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.rowitem, viewGroup, false)
                val viewHolder = ViewHolder(view)

                //item click listener
                viewHolder.setOnClickListener(object : ClickListener {
                    override fun onItemClick(view: View?, position: Int) {
                        //gettting data from firebase from the position clicked.
                        val mTitle = getItem(position).title
                        val mDesc = getItem(position).description
                        val mImage = getItem(position).image
                        //String postId = getItem(position).getUid();

                        //passing data to the new activity
                        val intent = Intent(view?.context, ImageDetailsActivity::class.java)
                        intent.putExtra("title", mTitle) //put title
                        intent.putExtra("description", mDesc) //put description
                        intent.putExtra("image", mImage) //put image url
                        // intent.putExtra("uid",postId);//put post id
                        startActivity(intent)
                    }

                    override fun onItemLongClick(view: View?, position: Int) {
                        //get the current title
                        val cTitle = getItem(position).title


                        //get the current description
                        val cDesc = getItem(position).description

                        //get the current image url
                        val cImageUrl = getItem(position).image
                        //get the current postId
                        val cImageId = getRef(position).key


                        //show dialog on long click
                        val builder = AlertDialog.Builder(this@MainActivity)

                        //options to display in a dialog
                        val options = arrayOf("Update", "Delete")

                        //setting dialog
                        builder.setItems(options) { dialog, which ->
                            //handle dialogs item clicks
                            if (which == 0) {
                                //update clicked
                                //start activity with putting the current data
                                val intent = Intent(this@MainActivity, ImageAddActivity::class.java)
                                intent.putExtra("cTitle", cTitle)
                                intent.putExtra("cDesc", cDesc)
                                intent.putExtra("cImageUrl", cImageUrl)
                                intent.putExtra("cImageId", cImageId)
                                startActivity(intent)
                            }
                            if (which == 1) {
                                //delete click
                                //method call
                                showDeleteDataDialog(cTitle, cImageUrl, cImageId)
                            }
                        }
                        builder.create().show() //show dialog
                    }
                })
                return viewHolder
            }
        }
        //set layout as a linear layout
        mRecyclerview!!.layoutManager = mLayoutManager
        (firebaseRecyclerAdapter as FirebaseRecyclerAdapter<ModelClass, ViewHolder>).startListening()

        //set adapter to firebase recyclerview
        mRecyclerview!!.adapter = firebaseRecyclerAdapter



    }

    //load data to recyclerview on start
    override fun onStart() {
        super.onStart()
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter!!.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter!!.stopListening()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //inflating the menu ,this adds items to the action bar if presenent
        menuInflater.inflate(R.menu.menu, menu)
        val menuItem = menu.findItem(R.id.menu_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = "Search by roads..."
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                firebaseSearch(s)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                //filter as you text
                firebaseSearch(s)
                return false
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        //handle other action bar item clicks hear
        if (id == R.id.menu_sorting) {
            //display alert dialog to choose sorting
            sortDialog()
            return true
        } else if (id == R.id.menu_logout) {
            //signout
            AuthUI.getInstance().signOut(this)
        } else  //start Add Post Activity
            if (id == R.id.add_post) {
                startActivity(Intent(this@MainActivity, ImageAddActivity::class.java))
            }
        return super.onOptionsItemSelected(item)
    }

    private fun sortDialog() {
        val sortingOptions = arrayOf("Newest", "Oldest")
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Sort By") //set title
            .setIcon(R.drawable.ic_action_sort) //set icon
            .setItems(sortingOptions) { dialogInterface, i ->
                //the i is the is the index position of the selected item
                //0 represents = sort by newest and 1 = sort by oldest
                if (i == 0) {
                    //sort by newest
                    //edit our shared preferences
                    val editor = mSharedPreferences!!.edit()
                    editor.putString("Sort", "newest") //where "sort"= key and newest=value
                    editor.apply() //apply/save our values to the shared preference
                    recreate() //restart the activity to take effect.
                } else if (i == 1) {
                    //sort by oldest
                    //edit our shared preferences
                    val editor = mSharedPreferences!!.edit()
                    editor.putString("Sort", "oldest") //where "sort"= key and oldest=value
                    editor.apply() //apply/save our values to the shared preference
                    recreate() //restart the activity to take effect.
                }
            }
        alertDialog.show()
    }

    companion object {
        // Choose an arbitrary request code value
        const val RC_SIGN_IN = 123
    }
}