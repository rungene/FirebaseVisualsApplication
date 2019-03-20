package com.rungenes.firebasevisualsapplication;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_add);

     /*   ActionBar actionBar = getActionBar();
        actionBar.setTitle("Add New Post");*/

        addTitle = findViewById(R.id.addTitle);
        addDescription = findViewById(R.id.addDescription);
        addImage = findViewById(R.id.addImage);
        buttonUpload = findViewById(R.id.buttonUpload);

        //assign Firebasestorage intsance to storage reference object
        mStorageReference = FirebaseStorage.getInstance().getReference();

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

                //upload data to firebase
                uploadDataToFirebase();


            }
        });


        //progress dialog
        progressDialog =new ProgressDialog(ImageAddActivity.this);

    }

    private void uploadDataToFirebase() {

        //check if the filepathuri is empty or not

        if (filePathUri!=null){

            //setting progress bar title
            progressDialog.setTitle("Image Uploading");


            //create a second storageReference
            final StorageReference storageReference2 = mStorageReference.child(uploadStoragePath + System.currentTimeMillis()+"."+getFileExtenstion(filePathUri));


            //adding an on success listener to storageReference2

            storageReference2.putFile(filePathUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
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


                    } else {


                        Toast.makeText(ImageAddActivity.this, "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();

                    }
                }
            });
                    /*.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //get title
                            String mTitle = addTitle.getText().toString().trim();
                            //get description
                            String mDesc = addDescription.getText().toString().trim();

                            //dismiss the progress bar
                            progressDialog.dismiss();

                            Toast.makeText(ImageAddActivity.this, "Post Uploaded", Toast.LENGTH_SHORT).show();

                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mTitle,mDesc,taskSnapshot.get);



                        }
                    });*/
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
