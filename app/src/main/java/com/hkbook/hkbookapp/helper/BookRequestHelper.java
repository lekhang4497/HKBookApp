package com.hkbook.hkbookapp.helper;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lekha on 6/17/2018.
 */

public class BookRequestHelper {
    public static String getRequestString(String rawTitle) {
        String title = toTitleInUrl(rawTitle);
        String strTitle = "title=" + title;
        String strKey = "key=<Enter your goodreads api key>";
        String strFormat = "format=xml";
        String parameters = strFormat + "&" + strKey + "&" + strTitle;

        String url = "https://www.goodreads.com/book/title?" + parameters;
        Log.e("URL", url);
        return url;
    }

    public static String getRequestStringForISBN(String rawResult) {
        String isbn = getIsbnFromString(rawResult);
        String strKey = "key=<Enter your goodreads api key>";
        String strFormat = "format=xml";
        String parameters = strFormat + "&" + strKey;

        String url = "https://www.goodreads.com/book/isbn/" + isbn + "?" + parameters;
        Log.e("URL", url);
        return url;
    }

    private static String getIsbnFromString(String rawResult) {
        String isbn = null;
        int idx = rawResult.indexOf("ISBN");
        int startIdx = idx + 4;
        try {
            for (int i = idx + 4; i < rawResult.length() - 4; i++) {
                if (Character.isDigit(rawResult.charAt(i))) {
                    startIdx = i;
                    break;
                }
            }
            int endIdx = rawResult.indexOf('\n', startIdx);
            String rawIsbn = rawResult.substring(startIdx, endIdx);
            Log.d("RAW ISBN", rawIsbn);
            isbn = trimIsbn(rawIsbn).substring(0, 13);
            Log.d("ISBN", isbn);
        } catch (Exception e){
            Log.e("ISBN ERROR", e.getMessage());
        }
        return isbn;
    }

    private static String trimIsbn(String rawIsbn) {
        return rawIsbn.replace(" ", "").replace("-", "").trim();
    }

    public static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("Exception download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public static String toTitleInUrl(String title) {
        StringBuilder ret = new StringBuilder();
        String[] words = title.split(" ");
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            ret.append(word.toLowerCase());
            if (i != words.length - 1) {
                ret.append("+");
            }
        }
        return ret.toString();
    }
}
