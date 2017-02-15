package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

/**
 * Created by rafael on 31/01/17.
 */

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

//import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Lets user add new list item.
 */
public class AddListItemDialogFragment extends EditListDialogFragment {
    private ListView mListView;
    private ActiveListItemAdapter mAdapter;
    private HashMap<String, User> mSharedWithMap;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static AddListItemDialogFragment newInstance(ShoppingList shoppingList, String listId, HashMap<String, User> sharedWithMap) {
        AddListItemDialogFragment addListItemDialogFragment = new AddListItemDialogFragment();

        Bundle bundle = newInstanceHelper(shoppingList, R.layout.dialog_add_item, listId);
        bundle.putSerializable(Constants.KEY_SHARED_WITH_MAP, sharedWithMap);
        addListItemDialogFragment.setArguments(bundle);

        return addListItemDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mSharedWithMap = (HashMap) bundle.getSerializable(Constants.KEY_SHARED_WITH_MAP);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         **/
        return super.createDialogHelper(R.string.positive_button_add_list_item);
    }

    /**
     * Adds new item to the current shopping list
     */
    @Override
    protected void doListEdit() {
        String itemName = mEditTextForList.getText().toString();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String owner = sharedPref.getString(getString(R.string.email_shared_preferences_key), null);

        if (itemName != "") {
            ShoppingListItem shoppingListItem = new ShoppingListItem(itemName, owner);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL);
            DatabaseReference newItem = ref.child(Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS).child(mListId).push();

            String shoppingItemId = newItem.getKey();

            HashMap<String, Object> updateShoppingList = new HashMap<>();

            HashMap<String, Object> itemToAdd = (HashMap<String, Object>) new ObjectMapper().convertValue(shoppingListItem, Map.class);

//            HashMap<String, Object> itemToAdd = new HashMap<>();
//            itemToAdd.put("itemName", itemName);
//            itemToAdd.put("owner", "Anonymous Owner");

            updateShoppingList.put(Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS+"/"+mListId+"/"+shoppingItemId, itemToAdd);
            Utils.updateMapWithTimestampLastChanged(mListId, owner, updateShoppingList, mSharedWithMap);

            ref.updateChildren(updateShoppingList, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Utils.updateTimestampLastChangedReverse(databaseError, mListId, owner, mSharedWithMap);
                }
            });
            AddListItemDialogFragment.this.getDialog().cancel();
        }

    }
}