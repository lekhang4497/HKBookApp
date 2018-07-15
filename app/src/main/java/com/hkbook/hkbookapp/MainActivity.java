package com.hkbook.hkbookapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements
        NavigationView.OnNavigationItemSelectedListener,
        BookFragment.OnListFragmentInteractionListener,
        SearchDialogFragment.NoticeDialogListener {

    // Recognize methods
    // TITLE: recognize by title image
    // ISBN: recognize by scanning isbn
    private static final String ARG_RECOGNIZE_METHOD = "recognize-method";
    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabaseCurrentUsers;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserName;
    // Binding
    private TextView mTxtUsername;
    private TextView mTxtUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // floating button clicked
                Intent photoActIntent = new Intent(MainActivity.this, ShowPhotoActivity.class);
                startActivity(photoActIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View headerLayout = navigationView.getHeaderView(0);

        // Get current user
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        // Binding username and user email
        mTxtUsername = headerLayout.findViewById(R.id.txtUsername);
        mTxtUserEmail = headerLayout.findViewById(R.id.txtUserEmail);
        mTxtUserEmail.setText(mCurrentUser.getEmail());
        mDatabaseCurrentUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mDatabaseCurrentUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCurrentUserName = dataSnapshot.child("Name").getValue().toString();
                mTxtUsername.setText(mCurrentUserName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Check authentication
        checkAuthentication();
    }

    private void checkAuthentication() {
        mAuth = FirebaseAuth.getInstance();
        //Check authentication
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_recognize) {
            Intent intent = new Intent(MainActivity.this, ShowPhotoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_author) {
            Intent intent = new Intent(MainActivity.this, ShowAuthorPhotoActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_search_book) {
            showSearchDialog();
        } else if (id == R.id.nav_sign_out) {
            mAuth.signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSearchDialog() {
        DialogFragment newFragment = new SearchDialogFragment();
        newFragment.show(getSupportFragmentManager(), "missiles");
    }

    @Override
    public void onListFragmentInteraction(Book item) {
        Intent intent = new Intent(MainActivity.this, ShowBookActivity.class);
        Log.d("BOOKKK", item.getDescription().toString());
        intent.putExtra("BOOK",item);
        intent.putExtra("VIEW_ONLY", true);
        startActivity(intent);
    }

    @Override
    public void onDialogPositiveClick(View view) {
        EditText txtSearch = view.findViewById(R.id.txt_dialog_search);
        String searchStr = txtSearch.getText().toString();
        Toast.makeText(this, searchStr, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogNegativeClick(View view) {

    }
}
