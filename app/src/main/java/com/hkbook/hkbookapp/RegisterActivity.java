package com.hkbook.hkbookapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;

public class RegisterActivity extends AppCompatActivity {

    private EditText name,email,password;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name=(EditText) findViewById(R.id.editUsername);
        email =(EditText) findViewById(R.id.editEmail);
        password=(EditText) findViewById(R.id.editPassword);
        progressBar=findViewById(R.id.pbLoading);

        mAuth=FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public void signupBtnClicked(View view){
        progressBar.setVisibility(View.VISIBLE);
        final String nameContent,passContent,emailContent;
        nameContent=name.getText().toString().trim();
        passContent=password.getText().toString().trim();
        emailContent=email.getText().toString().trim();
        if (!TextUtils.isEmpty(emailContent)&&!TextUtils.isEmpty(passContent)){
            mAuth.createUserWithEmailAndPassword(emailContent,passContent).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        progressBar.setVisibility(View.INVISIBLE);
                        String userID=mAuth.getCurrentUser().getUid();
                        Log.d("STATE",userID);
                        DatabaseReference userDb=mDatabase.child(userID);
                        userDb.child("Name").setValue(nameContent);
                        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(RegisterActivity.this, "Register fail", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Email, username and password can not be empty", Toast.LENGTH_SHORT).show();
        }
    }
}
