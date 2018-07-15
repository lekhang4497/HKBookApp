package com.hkbook.hkbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail;
    private EditText loginPass;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.editEmail);
        loginPass= findViewById(R.id.editPassword);
        loginProgress=findViewById(R.id.pbLogin);
        mAuth =FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void loginBtnClicked(View view){
        loginProgress.setVisibility(View.VISIBLE);
        String email=loginEmail.getText().toString().trim();
        String pass=loginPass.getText().toString().trim();

        if (!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(pass)){
            mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        checkUserExists();
                    }
                    else{
                        loginProgress.setVisibility(View.INVISIBLE);
                        Toast.makeText(LoginActivity.this, "Your email or password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(this, "Email and password can not be empty", Toast.LENGTH_SHORT).show();
            loginProgress.setVisibility(View.INVISIBLE);
        }
    }

    public void checkUserExists(){
        final String userID=mAuth.getCurrentUser().getUid();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userID)){
                    loginProgress.setVisibility(View.INVISIBLE);
                    Intent loginIntent=new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(loginIntent);
                }
                else{
                    loginProgress.setVisibility(View.INVISIBLE);
                    Toast.makeText(LoginActivity.this, "Your email or password is incorrect", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void registerBtnClicked(View view) {
        startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
    }
}
