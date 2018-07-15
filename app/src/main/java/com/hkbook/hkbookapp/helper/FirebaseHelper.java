package com.hkbook.hkbookapp.helper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hkbook.hkbookapp.Book;

/**
 * Created by lekha on 6/21/2018.
 */

public class FirebaseHelper {

    //Firebase
    private static FirebaseAuth mAuth;
    private static FirebaseAuth.AuthStateListener mAuthListener;
    private static DatabaseReference mDatabaseCurrentUsers;
    private static FirebaseUser mCurrentUser;

    public static void saveBook(Book book){
        // Get current user
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseCurrentUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        DatabaseReference newBook = mDatabaseCurrentUsers.child("Books").child(book.getIsbn());
        newBook.child("author").setValue(book.getAuthor().getName());
        newBook.child("imageUrl").setValue(book.getImageUrl());
        newBook.child("title").setValue(book.getTitle());
        newBook.child("goodreadsUrl").setValue(book.getGoodreadsUrl());
        newBook.child("description").setValue(book.getDescription());
    }
}
