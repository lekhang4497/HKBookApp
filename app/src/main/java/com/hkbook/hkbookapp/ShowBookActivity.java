package com.hkbook.hkbookapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hkbook.hkbookapp.helper.BookRequestHelper;
import com.hkbook.hkbookapp.helper.FirebaseHelper;
import com.hkbook.hkbookapp.helper.ImageHelper;
import com.hkbook.hkbookapp.helper.XmlParserHelper;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.LanguageCodes;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Random;

public class ShowBookActivity extends AppCompatActivity {

    private static final int REQUEST_RECOGNIZE_BOOK = 1;

    private TextView mTxtDescription;
    private ImageView mIvRegconize;
    private TextView mTxtTitle;
    private TextView mTxtAuthor;
    private Button mSaveButton;
    private Button mCancelButton;
    private Button mGoUrlButton;
    private String mMethod = "TITLE"; // Default method is recognizing by title image
    // The URI of the image selected to detect.
    private Uri mImageUri;
    private Uri mCroppedImageUri;
    // The image selected to detect.
    private Bitmap mBitmap;
    private VisionServiceClient client;
    private String mResult;
    private ProgressBar mProgressBar;
    // Recognized book
    private Book mBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_book);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }

        // Binding
        mTxtDescription = findViewById(R.id.txtBookDescription);
        mTxtAuthor = findViewById(R.id.txtBookAuthor);
        mTxtTitle = findViewById(R.id.txtBookTitle);
        mIvRegconize = findViewById(R.id.ivRecognize);
        mSaveButton = findViewById(R.id.btnSave);
        mCancelButton = findViewById(R.id.btnCancel);
        mGoUrlButton = findViewById(R.id.btnGoUrl);
        mProgressBar = findViewById(R.id.pbRecog);
        // Set initial state
        mProgressBar.setVisibility(View.INVISIBLE);
        mSaveButton.setEnabled(false);

        // Get intent
        Intent dataIntent = getIntent();
        Bundle extras = dataIntent.getExtras();
        if (extras != null) {
            mBook = extras.getParcelable("BOOK");
            if (mBook != null) {
                showBookInformation(mBook);
                mSaveButton.setEnabled(true);
            }
            boolean viewOnly = extras.getBoolean("VIEW_ONLY");
            if (viewOnly) {
                mSaveButton.setVisibility(View.INVISIBLE);
                mCancelButton.setVisibility(View.INVISIBLE);
                mGoUrlButton.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(this, "Recognize fail", Toast.LENGTH_SHORT).show();
            showRecognizeFail();
            mIvRegconize.setImageResource(R.drawable.icon_sad);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_RECOGNIZE_BOOK:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        mBook = extras.getParcelable("BOOK");
                        if (mBook != null) {
                            showBookInformation(mBook);
                            mSaveButton.setEnabled(true);
                        }
                    } else {
                        Toast.makeText(this, "Recognize fail", Toast.LENGTH_SHORT).show();
                        showRecognizeFail();
                        mIvRegconize.setImageResource(R.drawable.icon_sad);
                    }

                }
                break;
        }
    }

    public void onSaveBtnClicked(View view) {
        FirebaseHelper.saveBook(mBook);
        Intent intent = new Intent(ShowBookActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public void onGoUrlBtnClicked(View view) {
        String url = mBook.getGoodreadsUrl();
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browse);
    }

    public void onCancelBtnClicked(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void showRecognizeFail() {
        String fail = "Recognize fail";
        mTxtDescription.setText(fail);
        mTxtAuthor.setText(fail);
        mTxtTitle.setText(fail);
    }

    private void showBookInformation(Book book) {
        Picasso.get().load(book.getImageUrl()).into(mIvRegconize);
        mTxtTitle.setText(book.getTitle());
        mTxtAuthor.setText(book.getAuthor().getName());
        String description = book.getDescription();
        description = description.replace("<br />", "\n");
        mTxtDescription.setText(description);
    }
}
