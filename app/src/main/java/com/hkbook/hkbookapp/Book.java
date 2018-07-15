package com.hkbook.hkbookapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lekha on 6/17/2018.
 */

public class Book implements Parcelable {
    private String title;
    private Author author;
    private String isbn;
    private String imageUrl;
    private String description;
    private String goodreadsUrl;

    public Book() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGoodreadsUrl() {
        return goodreadsUrl;
    }

    public void setGoodreadsUrl(String goodreadsUrl) {
        this.goodreadsUrl = goodreadsUrl;
    }

    protected Book(Parcel in) {
        title = in.readString();
        author = (Author) in.readValue(Author.class.getClassLoader());
        isbn = in.readString();
        imageUrl = in.readString();
        description = in.readString();
        goodreadsUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeValue(author);
        dest.writeString(isbn);
        dest.writeString(imageUrl);
        dest.writeString(description);
        dest.writeString(goodreadsUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
