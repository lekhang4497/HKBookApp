package com.hkbook.hkbookapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lekha on 6/17/2018.
 */

public class Author implements Parcelable {
    private String name;

    protected Author(Parcel in) {
        name = in.readString();
    }

    public Author(){

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Author> CREATOR = new Parcelable.Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
