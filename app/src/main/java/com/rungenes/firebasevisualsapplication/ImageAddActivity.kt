package com.rungenes.firebasevisualsapplication

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import com.google.firebase.storage.StorageReference
import android.app.ProgressDialog
import android.os.Bundle
import com.rungenes.firebasevisualsapplication.R
import com.squareup.picasso.Picasso
import com.google.firebase.storage.FirebaseStorage
import android.content.Intent
import com.rungenes.firebasevisualsapplication.ImageAddActivity
import com.google.android.gms.tasks.OnSuccessListener
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.google.firebase.storage.UploadTask
import com.rungenes.firebasevisualsapplication.MainActivity
import com.rungenes.firebasevisualsapplication.ImageUploadInfo
import com.google.firebase.storage.OnProgressListener
import android.content.ContentResolver
import android.webkit.MimeTypeMap
import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ImageAddActivity() : AppCompatActivity() {
    private var addTitle: EditText? = null
    private var addDescription: EditText? = null
    private var addImage: ImageView? = null
    private var buttonUpload: Button? = null

    //Folder path to Firebase storage.
    private val uploadStoragePath = "All_Image_Upload/"

    //root database name for firebase database
    private val uploadDatabasePath = "Data"

    //creating a Uri
    private var filePathUri: Uri? = null

    //creating a a database reference and and storage reference;
    private var mStorageReference: StorageReference? = null
    private var mDatabaseReference: DatabaseReference? = null

    //progress dialog
    private var progressDialog: ProgressDialog? = null
    var myToolbar: Toolbar? = null

    //intent data will be stored in this variables
    var cTitle: String? = null
    var cDesc: String? = null
    var cImageUrl: String? = null
    var cImageId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_add)
        myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)
        myToolbar!!.title = "Add New Post"

        /*ActionBar actionBar = getActionBar();
        actionBar.setTitle("Add New Post");*/


        // Get a support ActionBar corresponding to this toolbar
        val ab = supportActionBar

        // Enable the Up button
        ab!!.setDisplayHomeAsUpEnabled(true)
        ab.setTitle("Add New Post")
        addTitle = findViewById(R.id.addTitle)
        addDescription = findViewById(R.id.addDescription)
        addImage = findViewById(R.id.addImage)
        buttonUpload = findViewById(R.id.buttonUpload)


        //try and get data from intent if not null
        val intent = intent.extras
        if (intent != null) {

            /*There are two ways to come to this activity
            1) "Add" this is by clicking + button on the actionbar
            2) "Update"  which is diplayed by dialog displayed by long clicking
            -this statement will be ran if we come here the seconds way.
            * */
            //get and store data
            cTitle = intent.getString("cTitle")
            cDesc = intent.getString("cDesc")
            cImageUrl = intent.getString("cImageUrl")
            cImageId = intent.getString("cImageId")

            //set data into views
            addTitle?.setText(cTitle)
            addDescription?.setText(cDesc)
            Picasso.get().load(cImageUrl).into(addImage)

            //change the title of the action bar and button
            ab.setTitle("Update Post")
            buttonUpload?.setText(R.string.rename_button_upload)
        }


        //assign Firebasestorage intsance to storage reference object
        mStorageReference = FirebaseStorage.getInstance().reference

        //assign FirebaseDatabse with root database name
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(uploadDatabasePath)


        //add a listener to choose image
        addImage?.setOnClickListener(View.OnClickListener { //creating an intent
            val intent = Intent()
            //setting an intent to select image from a phone
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code)
        })

        //button click to upload data to firebase
        buttonUpload?.setOnClickListener(View.OnClickListener {
            /*if we came hear from "Add" title of button will be "Upload "
                    *
                    * if we came hear from "Update" title of button will be "Update"
                    * */
            if ((buttonUpload?.getText() == "Upload")) {

                //call method to upload data to firebase
                uploadDataToFirebase()
            } else {


                //start update
                startUpdate()
            }
        })


        //progress dialog
        progressDialog = ProgressDialog(this@ImageAddActivity)
    }

    private fun startUpdate() {
        //we will first delete the previous image
        // we will delete using its url which stored in cImage variable.
        progressDialog!!.setTitle("Updating...")
        progressDialog!!.show()
        deletePreviousImage()
    }

    private fun deletePreviousImage() {
        if (cImageUrl != null && uploadStoragePath.contains(cImageUrl!!)) {
            val pictureReference = FirebaseStorage.getInstance().getReferenceFromUrl(
                cImageUrl!!
            )
            pictureReference.delete().addOnSuccessListener { //deleted
                Toast.makeText(
                    this@ImageAddActivity,
                    "Previous image deleted...",
                    Toast.LENGTH_SHORT
                ).show()
                //upload new image and get its url
                uploadNewImage()
            }.addOnFailureListener { e -> //failed
                //get and show the error message
                Toast.makeText(this@ImageAddActivity, e.message, Toast.LENGTH_SHORT).show()
                progressDialog!!.dismiss()
            }
        } else {
            uploadNewImage()
        }
    }

    private fun uploadNewImage() {

        //image name
        val imageName = System.currentTimeMillis().toString() + ".png"
        val storageReference2 = mStorageReference!!.child(uploadStoragePath + imageName)

        //get image from bitmap
        val bitmap = (addImage!!.drawable as BitmapDrawable).bitmap
        val byteArrayOutputStream = ByteArrayOutputStream()

        //compress image
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        val uploadTask = storageReference2.putBytes(data)
        uploadTask.addOnSuccessListener { taskSnapshot -> //sucesss
            Toast.makeText(this@ImageAddActivity, "New image uploaded", Toast.LENGTH_SHORT).show()


            //get the url for new image url
            val taskUri = taskSnapshot.storage.downloadUrl
            while (!taskUri.isSuccessful);
            val downloadUri = taskUri.result

            //update the database with the new data
            updateDatabase(downloadUri.toString())
        }.addOnFailureListener { e -> //error. show the error message
            Toast.makeText(this@ImageAddActivity, e.message, Toast.LENGTH_SHORT).show()
            progressDialog!!.dismiss()
        }
    }

    private fun updateDatabase(s: String) {
        //new values to update to previous
        val title = addTitle!!.text.toString()
        val description = addDescription!!.text.toString()
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val databaseReference = firebaseDatabase.reference.child("Data")

        //String postId = databaseReference.getKey();


        // Query query = databaseReference.orderByChild("title").equalTo(cTitle);
        val query = databaseReference.orderByKey().equalTo(cImageId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                //update data
                for (ds: DataSnapshot in dataSnapshot.children) {
                    ds.ref.child("title").setValue(title)
                    ds.ref.child("search").setValue(title.lowercase())
                    ds.ref.child("description").setValue(description)
                    ds.ref.child("image").setValue(s)


                    /*
                    we are updating data according/which -matches to title in the post(s)
                    *Since title of many posts can be same, in that case it will update all those posts with the same title
                    * so we can add another field "id" to each post and instead of using "title" in orderByChild(),because of  id
                    * of each post will be different,to make "id" different you can use time stamp.
                    * */
                    //this child keys must be spelled the same as in your firebase db.
                    progressDialog!!.dismiss()
                    Toast.makeText(this@ImageAddActivity, "Database updated...", Toast.LENGTH_SHORT)
                        .show()

                    //start main activity after database updating
                    startActivity(Intent(this@ImageAddActivity, MainActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun uploadDataToFirebase() {

        //check if the filepathuri is empty or not
        if (filePathUri != null) {

            //setting progress bar title
            progressDialog!!.setTitle("Image Uploading")


            //create a second storageReference
            val storageReference2 = mStorageReference!!.child(
                uploadStoragePath + System.currentTimeMillis() + "." + getFileExtenstion(
                    filePathUri!!
                )
            )


            //adding an on success listener to storageReference2
            storageReference2.putFile(filePathUri!!)
                .addOnSuccessListener(OnSuccessListener { taskSnapshot ->
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadUri = uriTask.result


                    //get title
                    val mTitle = addTitle!!.text.toString().trim { it <= ' ' }
                    //get description
                    val mDesc = addDescription!!.text.toString().trim { it <= ' ' }

                    //dismiss the progress bar
                    progressDialog!!.dismiss()
                    Toast.makeText(this@ImageAddActivity, "Post Uploaded", Toast.LENGTH_SHORT)
                        .show()
                    val imageUploadInfo =
                        ImageUploadInfo(mTitle, mDesc, downloadUri.toString(), mTitle.toLowerCase())

                    //getting image upload id
                    val imageUploadId = mDatabaseReference!!.push().key

                    //adding image upload id's  child element into databaseReference
                    mDatabaseReference!!.child((imageUploadId)!!).setValue(imageUploadInfo)
                    finish()
                } //if something went wrong like network failure
                ).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(e: Exception) {
                    progressDialog!!.dismiss()
                    //show error toast
                    Toast.makeText(this@ImageAddActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }).addOnProgressListener(object : OnProgressListener<UploadTask.TaskSnapshot> {
                override fun onProgress(taskSnapshot: UploadTask.TaskSnapshot) {
                    progressDialog!!.setTitle("Uploading")
                    val progress =
                        (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                    Toast.makeText(this@ImageAddActivity, "Upload is$progress", Toast.LENGTH_SHORT)
                        .show()
                    println("Upload is $progress% done")
                }
            })
        } else {
            Toast.makeText(this, "Please select image and add name", Toast.LENGTH_SHORT).show()
        }
    }

    //method to get the  selected image extension from file path uri
    private fun getFileExtenstion(uri: Uri): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()

//return the file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == Image_Request_Code) && (
                    resultCode == RESULT_OK) && (
                    data != null) && (
                    data.data != null)
        ) {
            filePathUri = data.data
            try {

                //getting the selected image into bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePathUri)

                //setting bitmap to imageview
                addImage!!.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        //image request code
        private val Image_Request_Code = 5
    }
}