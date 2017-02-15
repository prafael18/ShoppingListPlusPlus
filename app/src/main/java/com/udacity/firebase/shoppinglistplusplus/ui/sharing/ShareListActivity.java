package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

/**
 * Created by rafael on 07/02/17.
 */

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails.ActiveListDetailsActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.firebaseui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Allows for you to check and un-check friends that you share the current list with
 */
public class ShareListActivity extends BaseActivity {
    private static final String LOG_TAG = ShareListActivity.class.getSimpleName();
    private ListView mListView;
    private String mCurrentUserEmail;
    private FriendAdapter mSharedUsersAdapter;
    private ShoppingList mShoppingList;
    private Query mSharedUsersQuery;
    private DatabaseReference mActiveListRef, mSharedWithRef;
    private ValueEventListener mActiveListRefListener, mSharedWithRefListener;
    private HashMap<String, User> mSharedWithUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_list);

        Intent intent = getIntent();
        String listId = intent.getStringExtra(ActiveListDetailsActivity.SHARE_ACTIVITY_KEY);
        initializeScreen();

        mCurrentUserEmail = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.email_shared_preferences_key), null);
        mSharedUsersQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS_FRIENDS).child(mCurrentUserEmail);

//        mSharedUsersQuery.keepSynced(true);

        mSharedUsersAdapter = new FriendAdapter(this, User.class, R.layout.single_user_item, mSharedUsersQuery, listId, mCurrentUserEmail);
        mListView.setAdapter(mSharedUsersAdapter);

        mActiveListRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS_LISTS).child(mCurrentUserEmail).child(listId);

        mSharedWithRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_SHARED_WITH).child(listId);

        mActiveListRefListener = mActiveListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);

                if(shoppingList != null) {
                    mShoppingList = shoppingList;
                    mSharedUsersAdapter.setShoppingList(shoppingList);
                } else {
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println ("Error: "+databaseError.getMessage());
            }
        });

        mSharedWithRefListener = mSharedWithRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSharedWithUsers = new HashMap<String, User>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    mSharedWithUsers.put(snapshot.getKey(), snapshot.getValue(User.class));
                }
                mSharedUsersAdapter.setSharedWithUsers(mSharedWithUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println ("Error: "+databaseError.getMessage());
            }
        });





        /**
         * Link layout elements from XML and setup the toolbar
         */

    }

    @Override
    public void onResume () {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedUsersAdapter.cleanup();
        mActiveListRef.removeEventListener(mActiveListRefListener);
        mSharedWithRef.removeEventListener(mSharedWithRefListener);
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        mListView = (ListView) findViewById(R.id.list_view_friends_share);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Launch AddFriendActivity to find and add user to current user's friends list
     * when the button AddFriend is pressed
     */
    public void onAddFriendPressed(View view) {
        Intent intent = new Intent(ShareListActivity.this, AddFriendActivity.class);
        startActivity(intent);
    }
}
