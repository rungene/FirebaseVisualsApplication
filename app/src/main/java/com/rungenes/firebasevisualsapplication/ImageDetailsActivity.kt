package com.rungenes.firebasevisualsapplication

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import android.graphics.Bitmap
import android.os.Bundle
import com.rungenes.firebasevisualsapplication.R
import com.squareup.picasso.Picasso
import android.os.Build
import android.content.pm.PackageManager
import com.rungenes.firebasevisualsapplication.ImageDetailsActivity
import android.graphics.drawable.BitmapDrawable
import android.app.WallpaperManager
import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import android.os.Environment
import android.view.View
import android.widget.Button
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ImageDetailsActivity : AppCompatActivity() {
    var textViewTitleDetails: TextView? = null
    var textViewDescriptionDetails: TextView? = null

    //https://github.com/chrisbanes/PhotoView zoooming in  and out
    var imageViewRowDetails: PhotoView? = null
    var buttonSave: Button? = null
    var buttonShare: Button? = null
    var buttonWall: Button? = null
    var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_details)


        //action bar
        val actionBar = supportActionBar
        //action bar tilte
        actionBar!!.setTitle("Image Details")
        //setting back button on action bar
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        //views are intitialized
        textViewTitleDetails = findViewById(R.id.textViewTitleDetails)
        textViewDescriptionDetails = findViewById(R.id.textViewDescriptionDetails)
        imageViewRowDetails = findViewById(R.id.photo_view)
        buttonSave = findViewById(R.id.buttonSave)
        buttonShare = findViewById(R.id.buttonShare)
        buttonWall = findViewById(R.id.buttonWall)

        //getting data from the intent
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")
        val image = intent.getStringExtra("image")


        //setting data to the views
        textViewTitleDetails?.setText(title)
        textViewDescriptionDetails?.setText(description)
        Picasso.get().load(image).into(imageViewRowDetails)


        //getting image from imageview as bitmap
        // bitmap = ((BitmapDrawable) imageViewRowDetails.getDrawable()).getBitmap();

        //onclick for save button
        buttonSave?.setOnClickListener(View.OnClickListener {
            //if os >= marshmallow we need permission to save image
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_DENIED
                ) {
                    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    //show a pop up to grant permissions
                    requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE)
                } else {

                    //permission already granted, save
                    saveImage()
                }
            } else {
                //system os is < than marshmallow
                saveImage()
            }
        })
        //onclick for share button
        buttonShare?.setOnClickListener(View.OnClickListener { shareImage() })

        //oclick for wallpaper button
        buttonWall?.setOnClickListener(View.OnClickListener { setImageWallpaper() })
    }

    private fun setImageWallpaper() {
        bitmap = (imageViewRowDetails!!.drawable as BitmapDrawable).bitmap
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        try {
            wallpaperManager.setBitmap(bitmap)
            Toast.makeText(this, "Wallpaper set!...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareImage() {
        try {
            bitmap = (imageViewRowDetails!!.drawable as BitmapDrawable).bitmap
            //get image title and description and save in string s
            val stringDetails = """
                ${textViewTitleDetails!!.text}
                ${textViewDescriptionDetails!!.text}
                """.trimIndent()
            val file = File(externalCacheDir, "sample.png")
            val fileOutputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            file.setReadable(true, false)

            //intent to share image and text
            val intent = Intent(Intent.ACTION_SEND)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.putExtra(Intent.EXTRA_TEXT, stringDetails) //put text
            intent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    this@ImageDetailsActivity,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    file
                )
            )
            /* intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile
                    (ImageDetailsActivity.this,this.getApplicationContext().getPackageName()
                            +"com.rungenes.firebasevisualsapplication.fileprovider",file));*/

            // intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));//
            intent.type = "image/png"
            startActivity(Intent.createChooser(intent, "Share Via"))
        } catch (e: Exception) {
            Toast.makeText(this, "Please await the image to load", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImage() {
        try {
            val currentPhotoPath: String

            //time stamp for image name
            bitmap = (imageViewRowDetails!!.drawable as BitmapDrawable).bitmap
            val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())

            //Path to external storage
            val path = Environment.getExternalStorageDirectory()


            //File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //create a folder named "MDS"
            val directory = File("$path/MDS/")
            directory.mkdirs()

            //image name
            val imageName = "$timeStamp.PNG"
            val file = File(directory, imageName)
            currentPhotoPath = file.absolutePath
            val out: OutputStream
            try {
                out = FileOutputStream(file)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                Toast.makeText(
                    this,
                    imageName + "Image saved gallery" + directory,
                    Toast.LENGTH_SHORT
                ).show()

                //add picture to Android Gallery
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val f = File(currentPhotoPath)
                val contentUri = Uri.fromFile(f)
                mediaScanIntent.data = contentUri
                this.sendBroadcast(mediaScanIntent)
            } catch (e: Exception) {

                //failed to save
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Please await the image to load", Toast.LENGTH_SHORT).show()
        }
    }

    //handling onback pressed (open previous activity)
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_CODE -> {

                //if the request code is cancelled the results arrays should be empty
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission is granted. save image
                    saveImage()
                } else {
                    //permission is denied
                    Toast.makeText(this, "Enable permission to save image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_CODE = 1
    }
}