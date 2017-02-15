package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

/**
 * Created by rafael on 07/02/17.
 */

import android.app.Activity;
import android.provider.ContactsContract;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.ui.firebaseui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Populates the list_view_friends_share inside ShareListActivity
 */
public class FriendAdapter extends FirebaseListAdapter<User> {
    private static final String LOG_TAG = FriendAdapter.class.getSimpleName();
    private HashMap <DatabaseReference, ValueEventListener> mLocationListenerMap;
    private String mListId, mEmail;
    private boolean mAddFriend;
    private ShoppingList mShoppingList;
    private HashMap<String, User> mSharedUsersList;
    private HashMap<DatabaseReference, ValueEventListener> mListenerMap;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public FriendAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                         Query ref, String listId, String email) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mListId = listId;
        this.mEmail = email;
        mListenerMap = new HashMap<>();
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_user_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, final User friend) {
        TextView userName = (TextView) view.findViewById(R.id.user_name);
        userName.setText(friend.getName());
        final ImageButton toggleShare = (ImageButton) view.findViewById(R.id.button_toggle_share);

        final DatabaseReference sharedWithUserRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_SHARED_WITH)
                .child(mListId)
                .child(friend.getEmail());

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ValueEventListener listener = sharedWithUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(User.class) != null) {
                    toggleShare.setImageResource(R.drawable.ic_shared_check);
                    toggleShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ref.updateChildren(updateFriendInSharedWith(false, friend), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Utils.updateTimestampLastChangedReverse(databaseError, mListId, mEmail, mSharedUsersList);
                                }
                            });
                        }
                    });
                }
                else {
                    toggleShare.setImageResource(R.drawable.icon_add_friend);
                    toggleShare.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ref.updateChildren(updateFriendInSharedWith(true, friend), new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    Utils.updateTimestampLastChangedReverse(databaseError, mListId, mEmail, mSharedUsersList);
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mListenerMap.put(sharedWithUserRef, listener);
    }

    /**
     * Public method that is used to pass ShoppingList object when it is loaded in ValueEventListener
     */
    public void setShoppingList(ShoppingList shoppingList) {
        mShoppingList = shoppingList;
    }

    /**
     * Public method that is used to pass SharedUsers when they are loaded in ValueEventListener
     */
    public void setSharedWithUsers(HashMap<String, User> sharedUsersList) {
        mSharedUsersList = sharedUsersList;
    }


    /**
     * This method does the tricky job of adding or removing a friend from the sharedWith list.
     * @param addFriend This is true if the friend is being added, false is the friend is being removed.
     * @param friendToAddOrRemove This is the friend to either add or remove
     * @return
     */
    private HashMap<String, Object> updateFriendInSharedWith(Boolean addFriend, User friendToAddOrRemove) {
        HashMap<String, Object> updateSharedWith = new HashMap<>();
        mShoppingList.updateTimeStampLastChanged();
        HashMap<String, Object> listToAdd = (HashMap<String, Object>) new ObjectMapper().convertValue(mShoppingList, Map.class);

        if (addFriend) {
            HashMap<String, Object> userToAddMap = (HashMap<String, Object>) new ObjectMapper().convertValue(friendToAddOrRemove, Map.class);
            updateSharedWith.put(Constants.FIREBASE_LOCATION_SHARED_WITH+ "/" + mListId + "/" + friendToAddOrRemove.getEmail(), userToAddMap);

            updateSharedWith.put(Constants.FIREBASE_LOCATION_USERS_LISTS+"/"+friendToAddOrRemove.getEmail()+"/" +mListId,  listToAdd);

        } else {
            updateSharedWith.put(Constants.FIREBASE_LOCATION_SHARED_WITH+ "/" + mListId + "/" + friendToAddOrRemove.getEmail(), null);
            updateSharedWith.put (Constants.FIREBASE_LOCATION_USERS_LISTS+"/"+friendToAddOrRemove.getEmail()+"/"+mListId, null);

        }

        for(User currentUser: mSharedUsersList.values()) {
            String emailCurrentUser = currentUser.getEmail();
            if (!emailCurrentUser.equals(friendToAddOrRemove.getEmail())) {
                updateSharedWith.put(Constants.FIREBASE_LOCATION_USERS_LISTS + "/" + emailCurrentUser + "/" + mListId, listToAdd);
            }
        }

        Utils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getOwner(), updateSharedWith, null);
        return updateSharedWith;
    }

    @Override
    public void cleanup() {
        super.cleanup();

        for (HashMap.Entry<DatabaseReference, ValueEventListener> listener : mListenerMap.entrySet()) {
            listener.getKey().removeEventListener(listener.getValue());
        }

    }
}
