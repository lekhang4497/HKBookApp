package com.hkbook.hkbookapp;

import android.os.AsyncTask;
import android.util.Log;

import com.hkbook.hkbookapp.helper.BookRequestHelper;

/**
 * Created by lekha on 6/22/2018.
 */

public class RecognizeIsbnActivity extends RecognizeBookActivity {
    @Override
    protected AsyncTask<String, Void, String> getDoProcessResultTask() {
        return new doProcessResultTask();
    }

    private class doProcessResultTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String resultText = url[0];
            String requestString = BookRequestHelper.getRequestStringForISBN(resultText);
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


}
