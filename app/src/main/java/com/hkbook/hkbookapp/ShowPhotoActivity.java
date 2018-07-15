package com.hkbook.hkbookapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hkbook.hkbookapp.helper.ImageHelper;
import com.hkbook.hkbookapp.helper.SelectImageActivity;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowPhotoActivity extends AppCompatActivity {

    // Recognize methods
    // TITLE: recognize by title image
    // ISBN: recognize by scanning isbn
    private static final String ARG_RECOGNIZE_METHOD = "recognize-method";
    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;
    protected static final int REQUEST_RECOGNIZE_BOOK = 2;
    protected static final int REQUEST_RECOGNIZE_AUTHOR = 3;

    private String mMethod = "TITLE"; // Default method is recognizing by title image
    // The URI of the image selected to detect.
    private Uri mImageUri;
    protected Uri mCroppedImageUri;
    // The image selected to detect.
    private Bitmap mBitmap;
    ImageView mImageView;
    CardView mPhotoCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_photo);
        mImageView = findViewById(R.id.iv_capture_photo);
        mPhotoCard = findViewById(R.id.cvPhotoCard);
        // Get intent
        String recognizeMethod = getIntent().getStringExtra(ARG_RECOGNIZE_METHOD);
        if (recognizeMethod != null) {
            mMethod = recognizeMethod;
        }
        selectImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();
                    mCroppedImageUri = mImageUri; // If not cropped yet
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        grantUriPermission("com.hkbook.hkbookapp", mImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.iv_capture_photo);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());
                    }
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    mCroppedImageUri = result.getUri();
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mCroppedImageUri, getContentResolver());
                    ImageView imageView = findViewById(R.id.iv_capture_photo);
                    imageView.setImageBitmap(mBitmap);
                    mPhotoCard.getLayoutParams().height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_RECOGNIZE_BOOK:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Book resultBook = extras.getParcelable("BOOK");
                        if (resultBook != null) {
                            Intent intent = new Intent(ShowPhotoActivity.this, ShowBookActivity.class);
                            intent.putExtra("BOOK", resultBook);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Recognize fail", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case REQUEST_RECOGNIZE_AUTHOR:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        Author author = extras.getParcelable("AUTHOR");
                        if (author != null) {
                            Intent intent = new Intent(ShowPhotoActivity.this, ShowAuthorActivity.class);
                            intent.putExtra("AUTHOR", author);
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Recognize fail", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            default:
                break;
        }
    }

    private void selectImage() {
        Intent intent;
        intent = new Intent(ShowPhotoActivity.this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    public void onBtnTakeImgClick(View view) {
        selectImage();
    }

    public void onBtnCropImgClick(View view) {
        cropImage();
    }

    private void cropImage() {
        CropImage.activity(mImageUri)
                .start(this);
    }

    public void onRecognizeBtnClick(View view) {
        Intent intent = RecognizorFactory.getRecognizorIntent(this, RecognizorFactory.RECOGNIZOR_BOOK_PHOTO);
        intent.putExtra("CROPPED_PHOTO", mCroppedImageUri.toString());
        startActivityForResult(intent, REQUEST_RECOGNIZE_BOOK);
    }

    public void onIsbnBtnClicked(View view) {
        Intent intent = RecognizorFactory.getRecognizorIntent(this, RecognizorFactory.RECOGNIZOR_ISBN);
        intent.putExtra("CROPPED_PHOTO", mCroppedImageUri.toString());
        startActivityForResult(intent, REQUEST_RECOGNIZE_BOOK);
    }
}
