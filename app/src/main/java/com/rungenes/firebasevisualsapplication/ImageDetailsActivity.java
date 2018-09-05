package com.rungenes.firebasevisualsapplication;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ImageDetailsActivity extends AppCompatActivity {

    TextView textViewTitleDetails, textViewDescriptionDetails;
    ImageView imageViewRowDetails;

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
        textViewDescriptionDetails = findViewById(R.id.textViewTitleDetails);
        imageViewRowDetails = findViewById(R.id.imageViewRowDetails);

        //getting data from the intent
        String image  = getIntent().getStringExtra("image");
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");


        //setting data to the views
        textViewTitleDetails.setText(title);
        textViewDescriptionDetails.setText(description);
        Picasso.get().load(image).into(imageViewRowDetails);



    }
    //handling onback pressed (open previous activity)


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
