package com.hkbook.hkbookapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ShowAuthorActivity extends AppCompatActivity {

    TextView mAuthorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_author);
        // Binding
        mAuthorName = findViewById(R.id.txt_author_name);
        // Get extra
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            Author author = extras.getParcelable("AUTHOR");
            if (author != null){
                mAuthorName.setText(author.getName());
            }
        }
    }

    public void onCancelBtnClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onGoUrlBtnClicked(View view) {
    }
}
