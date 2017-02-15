package com.udacity.firebase.shoppinglistplusplus.utils;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Utility class
 */
public class Utils {
    /**
     * Format the date with SimpleDateFormat
     */
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private Context mContext = null;


    /**
     * Public constructor that takes mContext for later use
     */
    public Utils(Context con) {
        mContext = con;
    }

    public static HashMap<String, Object> updateMapForAllWithValue
            (final String listId,
             final String owner, HashMap<String, Object> mapToUpdate,
             String propertyToUpdate, Object valueToUpdate, HashMap<String, User> usersSharedWith) {

        mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USERS_LISTS + "/" + owner + "/"
                + listId + "/" + propertyToUpdate, valueToUpdate);

        if(usersSharedWith != null) {
            for (User currentUser : usersSharedWith.values()) {
                mapToUpdate.put("/" + Constants.FIREBASE_LOCATION_USERS_LISTS + "/" + currentUser.getEmail() + "/" + listId + "/" + propertyToUpdate, valueToUpdate);
            }
        }

        return mapToUpdate;
    }

    public static HashMap<String, Object> updateMapWithTimestampLastChanged
            (final String listId,
             final String owner, HashMap<String, Object> mapToAddDateToUpdate, HashMap<String, User> usersSharedWith) {
        /**
         * Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap
         */
        HashMap<String, Object> timestampNowHash = new HashMap<>();
        timestampNowHash.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);


        updateMapForAllWithValue(listId, owner, mapToAddDateToUpdate,
                Constants.FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED, timestampNowHash, usersSharedWith);


        return mapToAddDateToUpdate;
    }

    public static void updateTimestampLastChangedReverse (DatabaseError error, final String listId, final String owner, final HashMap<String, User> usersSharedWith) {

        if (error != null) {
            System.out.println(error.getMessage());
        }
        else {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.FIREBASE_LOCATION_USERS_LISTS)
                    .child(owner)
                    .child(listId);

            final String property = Constants.FIREBASE_PROPERTY_TIMESTAMP_REVERSED + "/" + Constants.FIREBASE_PROPERTY_TIMESTAMP;
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ShoppingList list = dataSnapshot.getValue(ShoppingList.class);
                    Long timestampReversed = -1 * (list.getTimestampLastChangedLong());
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> mapToUpdate = new HashMap<String, Object>();
                    updateMapForAllWithValue(listId, owner, mapToUpdate, property, timestampReversed, usersSharedWith);
                    ref.updateChildren(mapToUpdate);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static String decodeEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

}
