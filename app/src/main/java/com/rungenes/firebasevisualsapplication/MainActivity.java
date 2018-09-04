package com.rungenes.firebasevisualsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerview;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerview = findViewById(R.id.recyclerView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Visual List");

        mRecyclerview.setHasFixedSize(true);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        //sending the query to the Firebase
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mfirebaseDatabase.getReference("Data");
    }

    //search the data
    private void firebaseSearch(String textSearch) {
        Query firebaseQuery = mRef.orderByChild("title").startAt(textSearch).endAt(textSearch + "\uf8ff");

        FirebaseRecyclerAdapter<ModelClass, ViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ModelClass, ViewHolder>(
                        ModelClass.class,
                        R.layout.rowitem,
                        ViewHolder.class,
                        firebaseQuery

                ) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, ModelClass model, int position) {


                        viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getImage(), model.getDescription());

                    }
                    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                        ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);


                        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                          /*    //views
                                TextView mTitle = findViewById(R.id.textViewTitle);
                                TextView mDesc = findViewById(R.id.textViewDescription);
                                ImageView mImage = findViewById(R.id.imageViewRow);*/

                                //gettting data from firebase from the position clicked.
                                String mTitle = getItem(position).getTitle();
                                String mDesc = getItem(position).getDescription();
                                String mImage = getItem(position).getImage();


                                //passing data to the new activity
                                Intent intent = new Intent(view.getContext(), ImageDetailsActivity.class);

                                intent.putExtra("title", mTitle);//put title
                                intent.putExtra("description", mDesc);//put description
                                intent.putExtra("image", mImage);//put image url
                                startActivity(intent);


                            }

                            @Override
                            public void onItemLongClick(View view, int position) {

                            }
                        });


                        return viewHolder;
                    }


                };
        //set adapter to recyclerview
        mRecyclerview.setAdapter(firebaseRecyclerAdapter);

    }


    //load data to recyclerview on start

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ModelClass, ViewHolder> firebaseRecyclerAdapter = new
                FirebaseRecyclerAdapter<ModelClass, ViewHolder>(
                        ModelClass.class,
                        R.layout.rowitem,
                        ViewHolder.class,
                        mRef

                ) {
                    @Override
                    protected void populateViewHolder(ViewHolder viewHolder, ModelClass model, int position) {
                        viewHolder.setDetails(getApplicationContext(), model.getTitle(), model.getImage(), model.getDescription());

                    }

                    @Override
                    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                        ViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);


                        viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                            /*    //views
                                TextView mTitle = findViewById(R.id.textViewTitle);
                                TextView mDesc = findViewById(R.id.textViewDescription);
                                ImageView mImage = findViewById(R.id.imageViewRow);*/

                                //gettting data from firebase from the position clicked.
                                String mTitle = getItem(position).getTitle();
                                String mDesc = getItem(position).getDescription();
                                String mImage = getItem(position).getImage();


                                //passing data to the new activity
                                Intent intent = new Intent(view.getContext(), ImageDetailsActivity.class);

                                intent.putExtra("title", mTitle);//put title
                                intent.putExtra("description", mDesc);//put description
                                intent.putExtra("image", mImage);//put image url
                                startActivity(intent);


                            }

                            @Override
                            public void onItemLongClick(View view, int position) {

                            }
                        });


                        return viewHolder;
                    }
                };

        //set adapter to recyclerview
        mRecyclerview.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflating the menu ,this adds items to the action bar if presenent

        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                firebaseSearch(s);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //filter as you text
                firebaseSearch(s);

                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //handle other action bar item clicks hear

        if (id == R.id.menu_settings) {

            //TODO
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
