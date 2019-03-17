package com.rungenes.firebasevisualsapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerview;
    private FirebaseDatabase mfirebaseDatabase;
    private DatabaseReference mRef;
    private LinearLayoutManager mLayoutManager;//sorting
    private SharedPreferences mSharedPreferences;//saving sorting settings
   // private Button btnLogout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Choose an arbitrary request code value
    public static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // FacebookSdk.sdkInitialize(getApplicationContext());



        mAuth = FirebaseAuth.getInstance();



    //    btnLogout = (Button) findViewById(R.id.btnLogout);


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


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user!= null){
                    //signed in user
                    onInitializeSignedin(user.getDisplayName());


                    //      startActivity(new Intent(MainActivity.this,LoginActivity.class));
                }else {
                    //user is signed out
                    onSignedOutCleanup();

                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()

                                    ))
                                    .build(),
                            RC_SIGN_IN);

                }
            }
        };


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthListener);
    }



    private void onInitializeSignedin(String displayName) {



    }

    private void onSignedOutCleanup(){




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
        }else if (id==R.id.menu_logout){
            //signout
            AuthUI.getInstance().signOut(this);

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
