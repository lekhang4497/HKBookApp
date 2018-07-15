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

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class RecognizeBookActivity extends AppCompatActivity {

    // Recognize methods
    // TITLE: recognize by title image
    // ISBN: recognize by scanning isbn
    private static final String ARG_RECOGNIZE_METHOD = "recognize-method";

    private String mMethod = "TITLE"; // Default method is recognizing by title image
    // The URI of the image selected to detect
    private Uri mCroppedImageUri;
    // The image selected to detect.
    private Bitmap mBitmap;
    private VisionServiceClient client;
    private String mResult;
    // Recognized book
    protected Book mBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }
        // Get intent
        mCroppedImageUri = Uri.parse(getIntent().getStringExtra("CROPPED_PHOTO"));
        String recognizeMethod = getIntent().getStringExtra(ARG_RECOGNIZE_METHOD);
        if (recognizeMethod != null){
            mMethod = recognizeMethod;
        }
        // Set image to detect
        mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                mCroppedImageUri, getContentResolver());
        doRecognize();
    }

    public void doRecognize() {
        try {
            getRecognizeTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
            returnFail();
        }
    }

    private String process() throws VisionServiceException, IOException {
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        OCR ocr;
        ocr = this.client.recognizeText(inputStream, LanguageCodes.AutoDetect, true);

        String result = gson.toJson(ocr);
        Log.d("result", result);

        return result;
    }

    public void onSaveBtnClicked(View view) {
        FirebaseHelper.saveBook(mBook);
        Intent intent = new Intent(RecognizeBookActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    // Factory method
    protected AsyncTask<String, String, String> getRecognizeTask(){
        return new doRecognizeTask();
    }

    protected AsyncTask<String, Void, String> getDoProcessResultTask(){
        return new doProcessResultTask();
    }

    public void onCancelBtnClicked(View view) {
        super.onBackPressed();
    }

    private class doRecognizeTask extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doRecognizeTask() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return process();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            if (e != null) {
                Log.e("error", e.getMessage());
                this.e = null;
                returnFail();
            } else {
                Gson gson = new Gson();
                OCR r = gson.fromJson(data, OCR.class);

                String result = "";
                for (Region reg : r.regions) {
                    for (Line line : reg.lines) {
                        for (Word word : line.words) {
                            result += word.text + " ";
                        }
                        result += "\n";
                    }
                    result += "\n\n";
                }
                mResult = result;
                Log.e("RESULT", result);
                // Get book information from GoodRead
                getDoProcessResultTask().execute(mResult);
            }
        }
    }

    private class doProcessResultTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String requestString = BookRequestHelper.getRequestString(url[0]);
            String data = "";
            try {
                data = BookRequestHelper.downloadUrl(requestString);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("Result", result);
            // Parse the XML response
            if (result != null) {
                parseBookInformation(result);
            } else {
                returnFail();
            }
        }
    }

    protected void parseBookInformation(String xml) {
        XmlParserHelper parserHelper = new XmlParserHelper();
        InputStream stream = new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8")));
        try {
            Book book = parserHelper.parseBook(stream);
            // Finish getting book's information
            mBook = book;
            if (book != null) {
                returnResult();
            } else {
                returnFail();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            returnFail();
        }
    }
    
    protected void returnResult(){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("BOOK",mBook);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    protected void returnFail(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
