package com.rungenes.firebasevisualsapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class HomeFragment : Fragment() {

    private var mRecyclerview: RecyclerView? = null
    private var options: FirebaseRecyclerOptions<ModelClass>? = null
    private var mRef: DatabaseReference? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<ModelClass, ViewHolder>? = null
    private var mLayoutManager: LinearLayoutManager? = null
    //Folder path to Firebase storage.
    private val uploadStoragePath = "All_Image_Upload/"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mRecyclerview = view.findViewById(R.id.recyclerView)

        mRecyclerview?.setHasFixedSize(true)
    }


    private fun showDeleteDataDialog(
        currentTitle: String?,
        currentImageUrl: String?,
        currentImageId: String?
    ) {

        //alert dialog
        val alertDialog = AlertDialog.Builder(requireContext())
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
            Toast.makeText(requireContext(), "id$currentImageId", Toast.LENGTH_SHORT).show()
            query1.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {

                        // String key = dataSnapshot.getKey();
                        ds.ref.removeValue() //remove values from firebase when title matches.
                    }
                    //show a toast that the post was removed successfully
                    Toast.makeText(
                        requireContext(),
                        "Post Deleted Successfully..",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //if anything goes wrong show error message.
                    Toast.makeText(requireContext(), databaseError.message, Toast.LENGTH_SHORT)
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
                        requireContext(),
                        "Image deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e -> //failure to delete
                    //something went wrong.
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
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
                    requireContext(),
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
                viewHolder.setOnClickListener(object : ViewHolder.ClickListener {
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
                        val builder = AlertDialog.Builder(requireContext())

                        //options to display in a dialog
                        val options = arrayOf("Update", "Delete")

                        //setting dialog
                        builder.setItems(options) { dialog, which ->
                            //handle dialogs item clicks
                            if (which == 0) {

                                //update clicked
                                //start activity with putting the current data
                                val intent = Intent(requireContext(), ImageAddActivity::class.java)
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

}