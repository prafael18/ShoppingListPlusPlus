package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

        import android.os.Bundle;
        import android.support.v7.widget.Toolbar;
        import android.widget.EditText;
        import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.firebaseui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Add Friend screen and functionality
 */
public class AddFriendActivity extends BaseActivity {
    private EditText mEditTextAddFriendEmail;
    private ListView mListViewAutocomplete;
    private AutocompleteFriendAdapter mUsersAdapter;
    private String mCurrentUserEmail;
    private boolean mIsNotCurrentUser, mIsNotAlreadyAdded;
    private Query mAutoCompleteQuery;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        mCurrentUserEmail = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.email_shared_preferences_key), null);

        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();

        Query usersQuery = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);
        if(usersQuery != null) {
            mUsersAdapter = new AutocompleteFriendAdapter(this, User.class, R.layout.single_autocomplete_item, usersQuery.orderByChild(Constants.FIREBASE_PROPERTY_EMAIL));
            mListViewAutocomplete.setAdapter(mUsersAdapter);
        }

        mListViewAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final User selectedUser = mUsersAdapter.getItem(position);
                final DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child(Constants.FIREBASE_LOCATION_USERS_FRIENDS)
                        .child(mCurrentUserEmail);

                mIsNotCurrentUser = mUsersAdapter.isNotCurrentUser(selectedUser, mCurrentUserEmail);

                if (mIsNotCurrentUser) {
                    DatabaseReference refToSelectedUser = ref.child(selectedUser.getEmail());
                    refToSelectedUser.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mIsNotAlreadyAdded = mUsersAdapter.isNotAlreadyAdded(dataSnapshot, selectedUser);
                            if (mIsNotAlreadyAdded) {
                                HashMap<String, Object> updateUsersFriends = new HashMap<String, Object>();
                                HashMap<String, Object> newUserFriend = (HashMap<String, Object>) new ObjectMapper().convertValue(selectedUser, Map.class);
                                updateUsersFriends.put(selectedUser.getEmail(), newUserFriend);
                                ref.updateChildren(updateUsersFriends);
                            }
                            else {
                                Toast.makeText(AddFriendActivity.this, "You\'ve already added this user", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println("Error: " + databaseError.getMessage());
                        }
                    });
                }
                else {
                    Toast.makeText(AddFriendActivity.this, "You can\'t add yourself as a friend!", Toast.LENGTH_LONG).show();
                }

            }
        });

        /**
         * Set interactive bits, such as click events/adapters
         */
        /**mEditTextAddFriendEmail.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
        });**/

        mEditTextAddFriendEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                System.out.printf("charSequence = %s, start = %d, count = %d and after = %d%n", charSequence.toString(), i, i1, i2);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                System.out.printf("charSequence = %s, start = %d, before = %d and count = %d%n", charSequence.toString(), i, i1, i2);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                System.out.printf("editable = %s%n", editable.toString());
                String input = editable.toString().toLowerCase();
                if (input.equals("") || input.length() < 2) {
                    mListViewAutocomplete.setAdapter(null);
                }
                else {
                    mAutoCompleteQuery = FirebaseDatabase.getInstance().getReference()
                            .child(Constants.FIREBASE_LOCATION_USERS)
                            .orderByKey()
                            .startAt(input)
                            .endAt(editable.toString()+"~")
                            .limitToFirst(5);
                    mUsersAdapter = new AutocompleteFriendAdapter(AddFriendActivity.this, User.class, R.layout.single_autocomplete_item, mAutoCompleteQuery);
                    mListViewAutocomplete.setAdapter(mUsersAdapter);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUsersAdapter.cleanup();
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        mListViewAutocomplete = (ListView) findViewById(R.id.list_view_friends_autocomplete);
        mEditTextAddFriendEmail = (EditText) findViewById(R.id.edit_text_add_friend_email);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
