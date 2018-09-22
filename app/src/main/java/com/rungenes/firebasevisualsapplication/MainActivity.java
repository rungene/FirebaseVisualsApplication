package com.rungenes.firebasevisualsapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
    private LinearLayoutManager mLayoutManager;//sorting
    private SharedPreferences mSharedPreferences;//saving sorting settings

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerview = findViewById(R.id.recyclerView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("MDS List");

       //since the default value is the newest so for the first time it will display newest post first.
        mSharedPreferences = getSharedPreferences("SortingSettings",MODE_PRIVATE);
        String mSorting = mSharedPreferences.getString("Sort","newest");//where no setting is selected
        //newest becomes the default

        if (mSorting.equals("newest")){
            mLayoutManager = new LinearLayoutManager(this);

            //this will load the items from the bottom means newest  first.
            mLayoutManager.setReverseLayout(true);
            mLayoutManager.setStackFromEnd(true);

        }else if (mSorting.equals("oldest")){
            mLayoutManager = new LinearLayoutManager(this);

            //this will load the items from the bottom means oldest  first.
            mLayoutManager.setReverseLayout(false);
            mLayoutManager.setStackFromEnd(false);

        }

        mRecyclerview.setHasFixedSize(true);
        mRecyclerview.setLayoutManager(mLayoutManager);

        //sending the query to the Firebase
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mfirebaseDatabase.getReference("Data");
        mRef.keepSynced(true);



    }

    //search the data
    private void firebaseSearch(String textSearch) {

        String query = textSearch.toLowerCase();

        Query firebaseQuery = mRef.orderByChild("search").startAt(query).endAt(query + "\uf8ff");

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
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint("Search by roads...");
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

        if (id == R.id.menu_sorting) {
            //display alert dialog to choose sorting

            sortDialog();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sortDialog() {

        String[] sortingOptions = {"Newest","Oldest"};

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Sort By")//set title
        .setIcon(R.drawable.ic_action_sort)//set icon
        .setItems(sortingOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //the i is the is the index position of the selected item
                //0 represents = sort by newest and 1 = sort by oldest

                if (i==0){
                    //sort by newest
                    //edit our shared preferences

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("Sort","newest");//where "sort"= key and newest=value
                    editor.apply();//apply/save our values to the shared preference
                    recreate();//restart the activity to take effect.

                }else if (i==1){
                    //sort by oldest
                    //edit our shared preferences

                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putString("Sort","oldest");//where "sort"= key and oldest=value
                    editor.apply();//apply/save our values to the shared preference
                    recreate();//restart the activity to take effect.

                }
            }
        });

        alertDialog.show();

    }
}
