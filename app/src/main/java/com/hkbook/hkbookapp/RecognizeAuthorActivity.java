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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hkbook.hkbookapp.helper.BookRequestHelper;
import com.hkbook.hkbookapp.helper.FirebaseHelper;
import com.hkbook.hkbookapp.helper.ImageHelper;
import com.hkbook.hkbookapp.helper.XmlParserHelper;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisInDomainResult;
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

public class RecognizeAuthorActivity extends AppCompatActivity {

    // The URI of the image selected to detect
    private Uri mCroppedImageUri;
    // The image selected to detect.
    private Bitmap mBitmap;
    private VisionServiceClient client;
    private String mResult;
    // Recognized book
    protected Book mBook;
    protected Author mAuthor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key), getString(R.string.subscription_apiroot));
        }
        // Get intent
        mCroppedImageUri = Uri.parse(getIntent().getStringExtra("CROPPED_PHOTO"));
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

        String model = "celebrities";

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisInDomainResult v = this.client.analyzeImageInDomain(inputStream, model);
        String result = gson.toJson(v);
        Log.d("result", result);

        return result;
    }

    public void onSaveBtnClicked(View view) {
        FirebaseHelper.saveBook(mBook);
        Intent intent = new Intent(RecognizeAuthorActivity.this, MainActivity.class);
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
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisInDomainResult result = gson.fromJson(data, AnalysisInDomainResult.class);

                // Decode the returned result
                // NOTE: this is different for each domain model
                JsonArray detectedCelebs = result.result.get("celebrities").getAsJsonArray();
                if (detectedCelebs.size() < 1) {
                    returnFail();
                    return;
                }
                JsonElement celebElement = detectedCelebs.get(0);
                    JsonObject celeb = celebElement.getAsJsonObject();
                    Log.e("CELEBRITY", celeb.toString());
                    mAuthor = new Author();
                    mAuthor.setName(celeb.get("name").getAsString());
                    returnResult();
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
        returnIntent.putExtra("AUTHOR",mAuthor);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    protected void returnFail(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}
