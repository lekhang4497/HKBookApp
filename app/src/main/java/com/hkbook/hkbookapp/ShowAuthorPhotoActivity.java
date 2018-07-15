package com.hkbook.hkbookapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class ShowAuthorPhotoActivity extends ShowPhotoActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_author_photo);
    }

    public void onBtnRecogAuthorClicked(View view) {
        Intent intent = RecognizorFactory.getRecognizorIntent(this, RecognizorFactory.RECOGNIZOR_AUTHOR);
        intent.putExtra("CROPPED_PHOTO", mCroppedImageUri.toString());
        startActivityForResult(intent, REQUEST_RECOGNIZE_AUTHOR);
    }
}
