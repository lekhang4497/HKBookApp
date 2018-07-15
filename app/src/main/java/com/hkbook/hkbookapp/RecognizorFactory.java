package com.hkbook.hkbookapp;

import android.content.Context;
import android.content.Intent;

/**
 * Created by lekha on 6/22/2018.
 */

public class RecognizorFactory {
    public static final int RECOGNIZOR_BOOK_PHOTO = 1;
    public static final int RECOGNIZOR_ISBN = 2;
    public static final int RECOGNIZOR_AUTHOR = 3;

    public static Class getRecognizorClass(int recognizorId) {
        switch (recognizorId) {
            case RECOGNIZOR_BOOK_PHOTO:
                return RecognizeBookActivity.class;
            case RECOGNIZOR_ISBN:
                return RecognizeIsbnActivity.class;
            case RECOGNIZOR_AUTHOR:
                return RecognizeAuthorActivity.class;
        }
        return RecognizeBookActivity.class;
    }

    public static Intent getRecognizorIntent(Context context, int recognizorId) {
        switch (recognizorId) {
            case RECOGNIZOR_BOOK_PHOTO:
                return new Intent(context, RecognizeBookActivity.class);
            case RECOGNIZOR_ISBN:
                return new Intent(context, RecognizeIsbnActivity.class);
            case RECOGNIZOR_AUTHOR:
                return new Intent(context, RecognizeAuthorActivity.class);
        }
        return new Intent(context, RecognizeBookActivity.class);
    }
}
