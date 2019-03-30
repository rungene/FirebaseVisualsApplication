package com.rungenes.firebasevisualsapplication;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ImageAddActivity extends AppCompatActivity {

  private   EditText addTitle,addDescription;
   private ImageView addImage;
   private Button buttonUpload;

    //Folder path to Firebase storage.
    private String uploadStoragePath = "All_Image_Upload/";

    //root database name for firebase database
   private String uploadDatabasePath = "Data";

    //creating a Uri
   private Uri filePathUri;

    //creating a a database reference and and storage reference;

    private StorageReference mStorageReference;

    private DatabaseReference mDatabaseReference;

    //progress dialog
   private ProgressDialog progressDialog;

    //image request code
    private static final int Image_Request_Code= 5;

    Toolbar myToolbar;


    //intent data will be stored in this variables

    String cTitle,cDesc,cImageUrl;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_add);

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        myToolbar.setTitle("Add New Post");

        /*ActionBar actionBar = getActionBar();
        actionBar.setTitle("Add New Post");*/



        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Add New Post");





        addTitle = findViewById(R.id.addTitle);
        addDescription = findViewById(R.id.addDescription);
        addImage = findViewById(R.id.addImage);
        buttonUpload = findViewById(R.id.buttonUpload);




        //try and get data from intent if not null

        Bundle intent = getIntent().getExtras();
        if (intent!=null){

            /*There are two ways to come to this activity
            1) "Add" this is by clicking + button on the actionbar
            2) "Update"  which is diplayed by dialog displayed by long clicking
            -this statement will be ran if we come here the seconds way.
            * */

            //get and store data
            cTitle = intent.getString("cTitle");
            cDesc = intent.getString("cDesc");
            cImageUrl = intent.getString("cImageUrl");

            //set data into views

            addTitle.setText(cTitle);
            addDescription.setText(cDesc);
            Picasso.get().load(cImageUrl).into(addImage);

            //change the title of the action bar and button
            ab.setTitle("Update Post");
            buttonUpload.setText(R.string.rename_button_upload);
        }


        //assign Firebasestorage intsance to storage reference object
        mStorageReference = getInstance().getReference();

        //assign FirebaseDatabse with root database name
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(uploadDatabasePath);



        //add a listener to choose image

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //creating an intent
                Intent intent = new Intent();
                //setting an intent to select image from a phone
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);

            }
        });

        //button click to upload data to firebase

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if we came hear from "Add" title of button will be "Upload "
                *
                * if we came hear from "Update" title of button will be "Update"
                * */

                if (buttonUpload.getText().equals("Upload")){

                    //call method to upload data to firebase
                    uploadDataToFirebase();

                }else {


                    //start update

                    startUpdate();

                }

            }
        });


        //progress dialog
        progressDialog =new ProgressDialog(ImageAddActivity.this);

    }

    private void startUpdate() {
        //we will first delete the previous image
        // we will delete using its url which stored in cImage variable.
        progressDialog.setTitle("Updating...");
        progressDialog.show();

        deletePreviousImage();
    }

    private void deletePreviousImage() {

     if (cImageUrl!=null&& uploadStoragePath.contains(cImageUrl)) {

         StorageReference pictureReference = getInstance().getReferenceFromUrl(cImageUrl);

         pictureReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
             @Override
             public void onSuccess(Void aVoid) {
                 //deleted
                 Toast.makeText(ImageAddActivity.this, "Previous image deleted...", Toast.LENGTH_SHORT).show();
                 //upload new image and get its url
                 uploadNewImage();
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 //failed
                 //get and show the error message
                 Toast.makeText(ImageAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                 progressDialog.dismiss();
             }
         });

     }else {
         uploadNewImage();
     }

    }

    private void uploadNewImage() {

        //image name
        String imageName = System.currentTimeMillis()+".png";

        StorageReference storageReference2 = mStorageReference.child(uploadStoragePath+imageName);

        //get image from bitmap
        Bitmap bitmap = ((BitmapDrawable)addImage.getDrawable()).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        //compress image
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        UploadTask uploadTask = storageReference2.putBytes(data);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //sucesss
                Toast.makeText(ImageAddActivity.this, "New image uploaded", Toast.LENGTH_SHORT).show();


                //get the url for new image url

                Task<Uri> taskUri = taskSnapshot.getStorage().getDownloadUrl();
                while (!taskUri.isSuccessful());
                Uri downloadUri = taskUri.getResult();

                //update the database with the new data
                updateDatabase(downloadUri.toString());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                //error. show the error message
                Toast.makeText(ImageAddActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();

            }
        });

    }

    private void updateDatabase(final String s) {
        //new values to update to previous

        final String title = addTitle.getText().toString();
        final String description = addDescription.getText().toString();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Data");

        Query query = databaseReference.orderByChild("title").equalTo(cTitle);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //update data
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().child("title").setValue(title);
                    ds.getRef().child("search").setValue(title.toLowerCase());
                    ds.getRef().child("description").setValue(description);
                    ds.getRef().child("image").setValue(s);

                    /*
                    we are updating data according/which -matches to title in the post(s)
                    *Since title of many posts can be same, in that case it will update all those posts with the same title
                    * so we can add another field "id" to each post and instead of using "title" in orderByChild(),because of  id
                    * of each post will be different,to make "id" different you can use time stamp.
                    * */

                    //this child keys must be spelled the same as in your firebase db.

                    progressDialog.dismiss();
                    Toast.makeText(ImageAddActivity.this, "Database updated...", Toast.LENGTH_SHORT).show();

                    //start main activity after database updating
                    startActivity(new Intent(ImageAddActivity.this,MainActivity.class));
                    finish();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void uploadDataToFirebase() {

        //check if the filepathuri is empty or not

        if (filePathUri!=null){

            //setting progress bar title
            progressDialog.setTitle("Image Uploading");


            //create a second storageReference
            final StorageReference storageReference2 = mStorageReference.child(uploadStoragePath + System.currentTimeMillis()+"."+getFileExtenstion(filePathUri));


            //adding an on success listener to storageReference2

            storageReference2.putFile(filePathUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();


                    //get title
                    String mTitle = addTitle.getText().toString().trim();
                    //get description
                    String mDesc = addDescription.getText().toString().trim();

                    //dismiss the progress bar
                    progressDialog.dismiss();

                    Toast.makeText(ImageAddActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();

                    ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mTitle,mDesc,downloadUri.toString(),mTitle.toLowerCase());

                    //getting image upload id
                    String imageUploadId = mDatabaseReference.push().getKey();

                    //adding image upload id's  child element into databaseReference

                    mDatabaseReference.child(imageUploadId).setValue(imageUploadInfo);

                    finish();





                }
                //if something went wrong like network failure
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    progressDialog.dismiss();
                    //show error toast
                    Toast.makeText(ImageAddActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    progressDialog.setTitle("Uploading");

                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    Toast.makeText(ImageAddActivity.this, "Upload is"+progress, Toast.LENGTH_SHORT).show();

                    System.out.println("Upload is " + progress + "% done");
                }
            });

        /*continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();

                    }
                    return storageReference2.getDownloadUrl();


                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        // author.setText(downloadUri.toString());
                        Log.d("DownloadURL ",downloadUri.toString());


                        //get title
                        String mTitle = addTitle.getText().toString().trim();
                        //get description
                        String mDesc = addDescription.getText().toString().trim();

                        //dismiss the progress bar
                        progressDialog.dismiss();

                        Toast.makeText(ImageAddActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();

                        ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mTitle,mDesc,downloadUri.toString(),mTitle.toLowerCase());

                        //getting image upload id
                        String imageUploadId = mDatabaseReference.push().getKey();

                        //adding image upload id's  child element into databaseReference

                        mDatabaseReference.child(imageUploadId).setValue(imageUploadInfo);

                        finish();


                    } else {


                        Toast.makeText(ImageAddActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                }
            });*/


        }else {
            Toast.makeText(this, "Please select image and add name", Toast.LENGTH_SHORT).show();
        }

    }

    //method to get the  selected image extension from file path uri

    private String getFileExtenstion(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

//return the file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==Image_Request_Code &&
        resultCode==RESULT_OK &&
                data!=null&&
                data.getData()!=null
        ){

            filePathUri = data.getData();

            try {

                //getting the selected image into bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePathUri);

                //setting bitmap to imageview
                addImage.setImageBitmap(bitmap);


            }catch (Exception e){

                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }

        }
    }
}
