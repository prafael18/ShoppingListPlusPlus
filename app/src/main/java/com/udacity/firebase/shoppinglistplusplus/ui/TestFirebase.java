package com.udacity.firebase.shoppinglistplusplus.ui;

import android.provider.ContactsContract;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import static android.R.attr.order;

/**
 * Created by rafael on 31/01/17.
 */

public class TestFirebase {
    TestFirebase() {
    }

    public static void orderByKeyTest() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://dinosaur-facts.firebaseio.com/dinosaurs");
        Query queryRef = ref.orderByKey();
        queryRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChild) {
                System.out.println(snapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
            // ....
        });
    }


    public static void onChildAddedTest() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://docs-examples.firebaseio.com/web/saving-data/fireblog/posts");
        ref.addChildEventListener(new ChildEventListener() {
            // Retrieve new posts as they are added to the database
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                BlogPost newPost = snapshot.getValue(BlogPost.class);
                System.out.println("Author: " + newPost.getAuthor());
                System.out.println("Title: " + newPost.getTitle());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {

            }
//... ChildEventListener also defines onChildChanged, onChildRemoved,
            //    onChildMoved and onCanceled, covered in later sections.
        });

    }

}