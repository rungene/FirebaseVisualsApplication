package com.rungenes.firebasevisualsapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

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

    //Folder path to Firebase storage.
    private String uploadStoragePath = "All_Image_Upload/";


    private FirebaseRecyclerAdapter<ModelClass,ViewHolder>  firebaseRecyclerAdapter;
    private FirebaseRecyclerOptions<ModelClass> options;
    private FirebaseStorage firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // FacebookSdk.sdkInitialize(getApplicationContext());


        mAuth = FirebaseAuth.getInstance();



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


        //sending the query to the Firebase
        mfirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mfirebaseDatabase.getReference("Data");
        firebaseStorage = FirebaseStorage.getInstance();

        showData();
        mRef.keepSynced(true);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //signed in user
                    onInitializeSignedin(user.getDisplayName());


                    //      startActivity(new Intent(MainActivity.this,LoginActivity.class));
                } else {
                    //user is signed out
                    onSignedOutCleanup();



/*
                    ActionCodeSettings actionCodeSettings = ActionCodeSettings
                            .newBuilder()
                            .setAndroidPackageName("com.rungenes.firebasevisualsapplication", *//*installIfNotAvailable*//*false, *//*minimumVersion*//*null)
                            .setHandleCodeInApp(true)
                            .setUrl("https://imageuploadfirebase-5d152.firebaseapp.com") // This URL needs to be whitelisted
                            .build();*/

               /*     if (AuthUI.canHandleIntent(getIntent())) {
                        if (getIntent().getExtras() != null) {
                            return;
                        }
                        String link = getIntent().getExtras().getString(ExtraConstants.EMAIL_LINK_SIGN_IN);
                        if (link != null) {*/
   /*                         startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            //.setEmailLink(link)
                                            .setAvailableProviders(Arrays.asList(
                                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                    new AuthUI.IdpConfig.EmailBuilder().build())).build(),
                                    RC_SIGN_IN);

*/
                        createSignInIntent();
                        }



                }

                };






    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );
    public void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());

        //Create and Launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
    IdpResponse response = result.getIdpResponse();
    if (result.getResultCode() == RESULT_OK) {
        // Successfully signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Toast.makeText(this, "Signed in!" + user.getDisplayName(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sign-in successful but user data is unavailable.", Toast.LENGTH_SHORT).show();
        }

    } else if (result.getResultCode() == RESULT_CANCELED) {
        finish();
    } else {
        if (response == null){
            Toast.makeText(this, "Signing Cancelled", Toast.LENGTH_SHORT).show();
        } else {
            String str = "Error Code" + Objects.requireNonNull(response.getError()).getErrorCode();
            Toast.makeText(this, "Error Signing" + str, Toast.LENGTH_SHORT).show();
        }
    }
}
/*
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
    }*/




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


    private void showDeleteDataDialog(final String currentTitle, final String currentImageUrl, final String currentImageId) {

        //alert dialog
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Are you sure you want to delete this post?");
        //set positive/yes button
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //user pressed "Yes",delete data from firebase.
                /*whenever we publish a post the parent key is automatically created
                since we do not know the key for the items to remove we will first need to query the DB to
                * determine those keys.
                * */

                //getting image upload id
                //String imageUploadId = mRef.push().getKey();


               //Query query = mRef.orderByChild("title").equalTo(currentImageId);



                Query query1 = mRef.orderByKey().equalTo(currentImageId);

                Toast.makeText(MainActivity.this, "id"+currentImageId, Toast.LENGTH_SHORT).show();


                query1.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds: dataSnapshot.getChildren() ){

                            // String key = dataSnapshot.getKey();
                            ds.getRef().removeValue();//remove values from firebase when title matches.
                        }
                        //show a toast that the post was removed successfully

                        Toast.makeText(MainActivity.this, "Post Deleted Successfully..", Toast.LENGTH_SHORT).show();


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //if anything goes wrong show error message.

                        Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

                // delete the image using a url reference from firebase storage

                if (currentTitle!=null && currentImageUrl!=null&& uploadStoragePath.contains(currentImageUrl)) {

                    StorageReference storageReferencePicture = getInstance().getReferenceFromUrl(currentImageUrl);

                    storageReferencePicture.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            //deleted successfully
                            Toast.makeText(MainActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failure to delete
                            //something went wrong.
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }



        });




        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //user pressed "No", just dismiss the dialog
                dialog.dismiss();

            }
        });

        //show dialog
        alertDialog.create().show();


    }

//show data

    private void showData (){

        options = new FirebaseRecyclerOptions.Builder<ModelClass>().setQuery(mRef,ModelClass.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ModelClass, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ModelClass model) {

                holder.setDetails(getApplicationContext(), model.getTitle(), model.getImage(), model.getDescription());

            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int i) {

                //inflating layout row.xml
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rowitem,viewGroup,false);

                ViewHolder viewHolder = new ViewHolder(view);

                //item click listener
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //gettting data from firebase from the position clicked.
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();

                        String mImage = getItem(position).getImage();
                        // String postId = getItem(position).getUid();




                        //passing data to the new activity
                        Intent intent = new Intent(view.getContext(), ImageDetailsActivity.class);

                        intent.putExtra("title", mTitle);//put title
                        intent.putExtra("description", mDesc);//put description
                        intent.putExtra("image", mImage);//put image url
                        // intent.putExtra("uid",postId);//put post id
                        startActivity(intent);



                    }



                    @Override
                    public void onItemLongClick(View view, int position) {

                        //get the current title
                        final String cTitle = getItem(position).getTitle();


                        //get the current description
                        final String cDesc = getItem(position).getDescription();

                        //get the current image url

                        final String cImageUrl = getItem(position).getImage();
                        //get the current postId

                        final String cImageId= getRef(position).getKey();

                        //show dialog on long click

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        //options to display in a dialog

                        String [] options = {"Update","Delete"};

                        //setting dialog
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //handle dialogs item clicks

                                if (which==0){

                                    //update clicked
                                    //start activity with putting the current data

                                    Intent intent = new Intent(MainActivity.this,ImageAddActivity.class);
                                    intent.putExtra("cTitle",cTitle);
                                    intent.putExtra("cDesc",cDesc);
                                    intent.putExtra("cImageUrl",cImageUrl);
                                    intent.putExtra("cImageId",cImageId);
                                    startActivity(intent);

                                } if (which==1){

                                    //delete click
                                    //method call

                                    showDeleteDataDialog(cTitle, cImageUrl,cImageId);

                                }

                            }
                        });

                        builder.create().show();//show dialog





                    }

                });

                return viewHolder;
            }
        };


        //set layout as a linear layout

        mRecyclerview.setLayoutManager(mLayoutManager);
        firebaseRecyclerAdapter.startListening();

        //set adapter to firebase recyclerview
        mRecyclerview.setAdapter(firebaseRecyclerAdapter);


    }

    //search the data
    private void firebaseSearch(String textSearch) {
        //convert string entered in serchview to lower case

        String query = textSearch.toLowerCase();


        Query firebaseQuery = mRef.orderByChild("search").startAt(query).endAt(query + "\uf8ff");



        options = new FirebaseRecyclerOptions.Builder<ModelClass>().setQuery(firebaseQuery,ModelClass.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ModelClass, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ModelClass model) {

                holder.setDetails(getApplicationContext(), model.getTitle(), model.getImage(), model.getDescription());


            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                //inflating layout row.xml
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.rowitem,viewGroup,false);

                ViewHolder viewHolder = new ViewHolder(view);

                //item click listener

                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //gettting data from firebase from the position clicked.
                        String mTitle = getItem(position).getTitle();
                        String mDesc = getItem(position).getDescription();
                        String mImage = getItem(position).getImage();
                        //String postId = getItem(position).getUid();

                        //passing data to the new activity
                        Intent intent = new Intent(view.getContext(), ImageDetailsActivity.class);

                        intent.putExtra("title", mTitle);//put title
                        intent.putExtra("description", mDesc);//put description
                        intent.putExtra("image", mImage);//put image url
                       // intent.putExtra("uid",postId);//put post id
                        startActivity(intent);


                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        //get the current title
                        final String cTitle = getItem(position).getTitle();


                        //get the current description
                        final String cDesc = getItem(position).getDescription();

                        //get the current image url

                        final String cImageUrl = getItem(position).getImage();
                        //get the current postId

                        final String cImageId= getRef(position).getKey();


                        //show dialog on long click

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                        //options to display in a dialog

                        String [] options = {"Update","Delete"};

                        //setting dialog
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //handle dialogs item clicks

                                if (which==0){
                                    //update clicked
                                    //start activity with putting the current data
                                    Intent intent = new Intent(MainActivity.this,ImageAddActivity.class);
                                    intent.putExtra("cTitle",cTitle);
                                    intent.putExtra("cDesc",cDesc);
                                    intent.putExtra("cImageUrl",cImageUrl);
                                    intent.putExtra("cImageId",cImageId);
                                    startActivity(intent);
                                }
                                if (which==1){
                                    //delete click
                                    //method call
                                        showDeleteDataDialog(cTitle, cImageUrl,cImageId);
                                    }


                            }
                        });

                        builder.create().show();//show dialog

                    }
                });

                return viewHolder;
            }
        };
        //set layout as a linear layout

        mRecyclerview.setLayoutManager(mLayoutManager);
        firebaseRecyclerAdapter.startListening();

        //set adapter to firebase recyclerview
        mRecyclerview.setAdapter(firebaseRecyclerAdapter);








/*
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
                          */
/*    //views
                                TextView mTitle = findViewById(R.id.textViewTitle);
                                TextView mDesc = findViewById(R.id.textViewDescription);
                                ImageView mImage = findViewById(R.id.imageViewRow);*//*


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
*/

    }

    //load data to recyclerview on start
    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseRecyclerAdapter!=null){
            firebaseRecyclerAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter!=null){
            firebaseRecyclerAdapter.stopListening();
        }
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

        } else //start Add Post Activity
            if (id == R.id.add_post) {
                startActivity(new Intent(MainActivity.this, ImageAddActivity.class));
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
