package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

/**
 * Created by rafael on 31/01/17.
 */

import android.app.DialogFragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.sharing.ShareListActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static android.R.attr.name;
import static android.R.id.edit;
import static android.R.id.list;


/**
 * Represents the details screen for the selected shopping list
 */
public class ActiveListDetailsActivity extends BaseActivity {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    public static String SHARE_ACTIVITY_KEY = "share_activity";
    private ListView mListView;
    private ShoppingList mShoppingList;
    private String mListId, mEmail;
    private ActiveListItemAdapter mAdapter;
    private ValueEventListener mEventListenerActiveLists, mEventListenerCurrentUser, mUsersSharedWithRefListener;
    private DatabaseReference mRefActiveLists, mCurrentUserRef, mUsersSharedWithRef;
    private Button mShoppingButton;
    private boolean mIsShopping;
    private User mCurrentUser;
    private ArrayList<String> mEmailsUsersShopping, mUserNamesShopping;
    private TextView mPeopleShoppingTextView;
    private MenuItem mRemove, mEdit, mShare;
    private HashMap<String, User> mUsersSharedWithMap;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_list_details);

        mEmail = PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.email_shared_preferences_key), null);

        mCurrentUserRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS).child(mEmail);

        Intent pushIdIntent = getIntent();
        if (pushIdIntent != null && pushIdIntent.hasExtra(Constants.PUSH_ID_KEY)) {
            mListId = pushIdIntent.getStringExtra(Constants.PUSH_ID_KEY);
        }

        initializeScreen();

        DatabaseReference refShoppingListItem = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_SHOPPING_LIST_ITEMS).child(mListId);

//        refShoppingListItem.keepSynced(true);


        Query orderByBought = refShoppingListItem.orderByChild(Constants.FIREBASE_PROPERTY_BOUGHT);
        mAdapter = new ActiveListItemAdapter(this, ShoppingListItem.class, R.layout.single_active_list_item, orderByBought, mListId, mEmail);
        if(mShoppingList != null) {
            mListView.setAdapter(mAdapter);
        }


        mUsersSharedWithRef = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_SHARED_WITH)
                .child(mListId);

        mUsersSharedWithRefListener = mUsersSharedWithRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUsersSharedWithMap = new HashMap<String, User>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mUsersSharedWithMap.put(snapshot.getKey(), snapshot.getValue(User.class));
                }
                mAdapter.setSharedWithMap(mUsersSharedWithMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error: " + databaseError.getMessage());
            }
        });



        mEventListenerCurrentUser = mCurrentUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser!=null) mCurrentUser = currentUser;
                else finish();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println(databaseError.getMessage());
            }
        });

        mRefActiveLists = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS_LISTS).child(mEmail).child(mListId);
        mEventListenerActiveLists = mRefActiveLists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShoppingList shoppingList = dataSnapshot.getValue(ShoppingList.class);

                if (shoppingList == null) {
                    finish();
                    return;
                }
                mShoppingList = shoppingList;

                String name = mShoppingList.getOwner();

                if (mEmail.equals(mShoppingList.getOwner())) {
                    mRemove.setVisible(true);
                    mEdit.setVisible(true);
                    mShare.setVisible(true);
                }

                mAdapter.setShoppingList(mShoppingList);
                mListView.setAdapter(mAdapter);
                invalidateOptionsMenu();

                mEmailsUsersShopping = new ArrayList<String>();
                mUserNamesShopping = new ArrayList<String>();
                Iterable<DataSnapshot> snapshot = dataSnapshot.child(Constants.FIREBASE_PROPERTY_USERS_SHOPPING).getChildren();
                for(DataSnapshot snap: snapshot) {
                    mEmailsUsersShopping.add(snap.getKey());
                    mUserNamesShopping.add(snap.child(Constants.FIREBASE_PROPERTY_USERNAME).getValue().toString());
                }

                HashMap<String, Object> usersShopping = mShoppingList.getUsersShopping();
                if (mShoppingList != null && usersShopping != null) {
                    if (usersShopping.containsKey(mEmail)) {
                        mIsShopping = true;
                        mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.dark_grey));
                        mShoppingButton.setText(getString(R.string.button_stop_shopping));
                    }
                    else {
                        mIsShopping = false;
                        mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
                        mShoppingButton.setText(getString(R.string.button_start_shopping));
                    }

                }
                else {
                    mIsShopping = false;
                    mShoppingButton.setBackgroundColor(ContextCompat.getColor(ActiveListDetailsActivity.this, R.color.primary_dark));
                    mShoppingButton.setText(getString(R.string.button_start_shopping));
                }

                setTitle(mShoppingList.getListName());
                mPeopleShoppingTextView.setText(setWhoIsShoppingText());
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(LOG_TAG, firebaseError.getMessage());
            }
        });



        /**
         * Set up click listeners for interaction.
         */

        /* Show edit list item name dialog on listView item long click event */
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                /* Check that the view is not the empty footer item */
                if(view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mAdapter.getItem(position);
                    if (shoppingListItem != null) {
                        if ((mEmail.equals(shoppingListItem.getOwner()) || mEmail.equals(mShoppingList.getOwner())) && mIsShopping && (!shoppingListItem.isBought())) {
                            String itemName = shoppingListItem.getItemName();
                            DatabaseReference ref = mAdapter.getRef(position);
                            showEditListItemNameDialog(ref.toString(), itemName);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(view.getId() != R.id.list_view_footer_empty) {
                    ShoppingListItem shoppingListItem = mAdapter.getItem(position);

                    if (mIsShopping) {
                        if (shoppingListItem.isBought()) {
                            if (shoppingListItem.getBuyerEmail().equals(mEmail)) {
//                                shoppingListItem.setBuyer(null, false);
                                DatabaseReference ref = mAdapter.getRef(position);
                                HashMap<String, Object> updateItem = new HashMap<String, Object>();
                                updateItem.put("bought", false);
                                updateItem.put("buyerEmail", null);
                                ref.updateChildren(updateItem);
                            }
                        }
                        else {
//                            shoppingListItem.setBuyer(mEmail, true);
                            HashMap<String, Object> updateItem = new HashMap<String, Object>();
//                            updateItem.put("owner", shoppingListItem.getOwner());
//                            updateItem.put("itemName", shoppingListItem.getItemName());
                            updateItem.put("bought", true);
                            updateItem.put("buyerEmail", mEmail);
                            DatabaseReference ref = mAdapter.getRef(position);
                            ref.updateChildren(updateItem);
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_list_details, menu);

        /**
         * Get menu items
         */
        mRemove = menu.findItem(R.id.action_remove_list);
        mEdit = menu.findItem(R.id.action_edit_list_name);
        mShare = menu.findItem(R.id.action_share_list);
        MenuItem archive = menu.findItem(R.id.action_archive);

        /* Only the edit and remove options are implemented */
        mRemove.setVisible(false);
        mEdit.setVisible(false);
        mShare.setVisible(true);
        archive.setVisible(false);

        if (mShoppingList != null) {
            if (mEmail.equals(mShoppingList.getOwner())) {
                mRemove.setVisible(true);
                mEdit.setVisible(true);
                mShare.setVisible(true);
            }
        }


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        /**
         * Show edit list dialog when the edit action is selected
         */
        if (id == R.id.action_edit_list_name) {
            showEditListNameDialog();
            return true;
        }

        /**
         * removeList() when the remove action is selected
         */
        if (id == R.id.action_remove_list) {
            removeList();
            return true;
        }

        /**
         * Eventually we'll add this
         */
        if (id == R.id.action_share_list) {
            Intent intent = new Intent (this, ShareListActivity.class);
            intent.putExtra(SHARE_ACTIVITY_KEY, mShoppingList.getShoppingListId());
            startActivity(intent);
            return true;
        }

        /**
         * archiveList() when the archive action is selected
         */
        if (id == R.id.action_archive) {
            archiveList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Cleanup when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mRefActiveLists.removeEventListener(mEventListenerActiveLists);
        mCurrentUserRef.removeEventListener(mEventListenerCurrentUser);
        mUsersSharedWithRef.removeEventListener(mUsersSharedWithRefListener);
        mAdapter.cleanup();
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    private void initializeScreen() {
        mShoppingButton = (Button) findViewById(R.id.button_shopping);
        mListView = (ListView) findViewById(R.id.list_view_shopping_list_items);
        mPeopleShoppingTextView = (TextView) findViewById(R.id.text_view_people_shopping);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        /* Common toolbar setup */
        setSupportActionBar(toolbar);
        /* Add back button to the action bar */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /* Inflate the footer, set root layout to null*/
        View footer = getLayoutInflater().inflate(R.layout.footer_empty, null);
        mListView.addFooterView(footer);
    }


    /**
     * Archive current list when user selects "Archive" menu item
     */
    public void archiveList() {
    }


    /**
     * Start AddItemsFromMealActivity to add meal ingredients into the shopping list
     * when the user taps on "add meal" fab
     */
    public void addMeal(View view) {
    }

    /**
     * Remove current shopping list and its items from all nodes
     */
    public void removeList() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = RemoveListDialogFragment.newInstance(mShoppingList, mListId, mUsersSharedWithMap);
        dialog.show(getFragmentManager(), "RemoveListDialogFragment");
    }

    /**
     * Show the add list item dialog when user taps "Add list item" fab
     */
    public void showAddListItemDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListItemDialogFragment.newInstance(mShoppingList, mListId, mUsersSharedWithMap);
        dialog.show(getFragmentManager(), "AddListItemDialogFragment");
    }

    /**
     * Show edit list name dialog when user selects "Edit list name" menu item
     */
    public void showEditListNameDialog() {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListNameDialogFragment.newInstance(mShoppingList, mListId, mUsersSharedWithMap);
        dialog.show(this.getFragmentManager(), "EditListNameDialogFragment");
    }

    /**
     * Show the edit list item name dialog after longClick on the particular item
     */
    public void showEditListItemNameDialog(String listItemRef, String itemName) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = EditListItemNameDialogFragment.newInstance(mShoppingList, mListId, listItemRef, itemName, mUsersSharedWithMap);
        dialog.show(this.getFragmentManager(), "EditListItemNameDialogFragment");
    }

    /**
     * This method is called when user taps "Start/Stop shopping" button
     */
    public void toggleShopping(View view) {
        HashMap<String, Object> updatedUserData = new HashMap<String, Object>();
        String propertyToUpdate = Constants.FIREBASE_PROPERTY_USERS_SHOPPING + "/" + mEmail;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        if (mIsShopping) {
            Utils.updateMapForAllWithValue(mListId, mShoppingList.getOwner(), updatedUserData, propertyToUpdate, null, mUsersSharedWithMap);
            Utils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getOwner(), updatedUserData, mUsersSharedWithMap);
            ref.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Utils.updateTimestampLastChangedReverse(databaseError, mListId, mShoppingList.getOwner(), mUsersSharedWithMap);
                }
            });
        }
        else {
            HashMap<String, Object> currentUser = (HashMap<String, Object>)
                    new ObjectMapper().convertValue(mCurrentUser, Map.class);

            Utils.updateMapForAllWithValue(mListId, mShoppingList.getOwner(), updatedUserData, propertyToUpdate, currentUser, mUsersSharedWithMap);
            Utils.updateMapWithTimestampLastChanged(mListId, mShoppingList.getOwner(), updatedUserData, mUsersSharedWithMap);
            ref.updateChildren(updatedUserData, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Utils.updateTimestampLastChangedReverse(databaseError, mListId, mShoppingList.getOwner(), mUsersSharedWithMap);
                }
            });
        }

    }

    private String setWhoIsShoppingText () {

        HashMap<String, Object> usersShopping = mShoppingList.getUsersShopping();

        if (usersShopping != null) {
            int usersCount = usersShopping.size();
            if (usersCount == 1) {
                if (usersShopping.containsKey(mEmail)) {
                    return getString(R.string.you_are_shopping);
                } else {
                    return String.format(getString(R.string.one_user_is_shopping), mUserNamesShopping.get(0));
                }
            } else if (usersCount == 2) {
                if (usersShopping.containsKey(mEmail)) {
                    if (mEmailsUsersShopping.get(0).equals(mEmail)) {
                        return String.format(getString(R.string.you_and_another_user_are_shopping), mUserNamesShopping.get(1));
                    } else {
                        return String.format(getString(R.string.you_and_another_user_are_shopping), mUserNamesShopping.get(0));
                    }
                } else {
                    return String.format(getString(R.string.two_other_users_are_shopping), mUserNamesShopping.get(0), mUserNamesShopping.get(1));
                }
            } else {
                if (usersShopping.containsKey(mEmail)) {
                    return String.format(getString(R.string.more_than_two_users_are_shopping), "You", usersCount-1);
                } else {
                    return String.format(getString(R.string.more_than_two_users_are_shopping), mUserNamesShopping.get(0), usersCount-1);
                }
            }
        }
        else {
            return "";
        }
    }



    private void showErrorToast(String message) {
        Toast.makeText(ActiveListDetailsActivity.this, message, Toast.LENGTH_LONG).show();
    }
}