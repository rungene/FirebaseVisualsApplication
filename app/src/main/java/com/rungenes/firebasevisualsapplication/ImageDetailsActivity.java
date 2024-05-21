package com.rungenes.firebasevisualsapplication;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ImageDetailsActivity extends AppCompatActivity {

    TextView textViewTitleDetails, textViewDescriptionDetails;

    //https://github.com/chrisbanes/PhotoView zoooming in  and out
    PhotoView imageViewRowDetails;
    Button buttonSave, buttonShare, buttonWall;
    Bitmap bitmap;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);


        //action bar

        ActionBar actionBar = getSupportActionBar();
        //action bar tilte
        actionBar.setTitle("Image Details");
        //setting back button on action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //views are intitialized

        textViewTitleDetails = findViewById(R.id.textViewTitleDetails);
        textViewDescriptionDetails = findViewById(R.id.textViewDescriptionDetails);

        imageViewRowDetails = findViewById(R.id.photo_view);

        buttonSave = findViewById(R.id.buttonSave);
        buttonShare = findViewById(R.id.buttonShare);
        buttonWall = findViewById(R.id.buttonWall);

        //getting data from the intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String image = getIntent().getStringExtra("image");



        //setting data to the views
        textViewTitleDetails.setText(title);
        textViewDescriptionDetails.setText(description);

        Picasso.get().load(image).into(imageViewRowDetails);








        //getting image from imageview as bitmap
       // bitmap = ((BitmapDrawable) imageViewRowDetails.getDrawable()).getBitmap();

        //onclick for save button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if os >= marshmallow we need permission to save image
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
/*                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

                        //show a pop up to grant permissions
                        requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE);

                    } else {

                        //permission already granted, save
                        saveImage();
                    }*/
                    checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE_CODE);


                } else {
                    //system os is < than marshmallow
                    saveImage();

                }

            }
        });
        //onclick for share button

        buttonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                shareImage();

            }
        });

        //oclick for wallpaper button
        buttonWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                setImageWallpaper();
            }
        });
    }
    // Function to check and request permission
    public void checkPermission(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(this, permission) ==
        PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {permission}, requestCode);
        } else {
            //permission already granted, save
            saveImage();
        }
    }

    private void setImageWallpaper() {



        bitmap = ((BitmapDrawable) imageViewRowDetails.getDrawable()).getBitmap();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        try {
            wallpaperManager.setBitmap(bitmap);

            Toast.makeText(this, "Wallpaper set!...", Toast.LENGTH_SHORT).show();


        }catch (Exception e){

            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void shareImage() {


        try {
            bitmap = ((BitmapDrawable) imageViewRowDetails.getDrawable()).getBitmap();
            //get image title and description and save in string s

            String stringDetails = textViewTitleDetails.getText().toString() + "\n" +textViewDescriptionDetails.getText().toString();
            File file = new File(getExternalCacheDir(),"sample.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            file.setReadable(true,false);

            //intent to share image and text
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT,stringDetails);//put text

            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ImageDetailsActivity.this,
                    BuildConfig.APPLICATION_ID +".fileprovider",
                    file));
           /* intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile
                    (ImageDetailsActivity.this,this.getApplicationContext().getPackageName()
                            +"com.rungenes.firebasevisualsapplication.fileprovider",file));*/

           // intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));//
            intent.setType("image/png");
            startActivity(Intent.createChooser(intent,"Share Via"));



        } catch (Exception e) {
            Toast.makeText(this,"Please await the image to load", Toast.LENGTH_SHORT).show();

        }

    }

    private void saveImage() {


        try {
            String currentPhotoPath;

            //time stamp for image name
            bitmap = ((BitmapDrawable) imageViewRowDetails.getDrawable()).getBitmap();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(System.currentTimeMillis());

            //Path to external storage
            File path = Environment.getExternalStorageDirectory();



            //File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //create a folder named "MDS"

            File directory = new File(path + "/MDS/");

            directory.mkdirs();

            //image name
            String imageName = timeStamp + ".PNG";



            File file = new File(directory, imageName);



            currentPhotoPath = file.getAbsolutePath();

            OutputStream out;

            try {
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
                Toast.makeText(this, imageName + "Image saved gallery" + directory, Toast.LENGTH_SHORT).show();

                //add picture to Android Gallery

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(currentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);


            } catch (Exception e) {

                //failed to save
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){

            Toast.makeText(this, "Please await the image to load", Toast.LENGTH_SHORT).show();
        }



    }
    //handling onback pressed (open previous activity)

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_CODE: {
                //if the request code is cancelled the results arrays should be empty
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission is granted. save image
                    saveImage();

                } else {
                    //permission is denied
                    Toast.makeText(this, "Enable permission to save image", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }

    }


}
