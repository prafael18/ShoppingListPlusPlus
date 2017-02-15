package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

/**
 * Created by rafael on 31/01/17.
 */

import android.app.Dialog;
import android.os.Bundle;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;

/**
 * Lets user edit list item name for all copies of the current list
 */
public class EditListItemNameDialogFragment extends EditListDialogFragment {
    private String mListItemRef;
    private String mPreviousItemName;
    private HashMap<String, User> mSharedWithMap;
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListItemNameDialogFragment newInstance(ShoppingList shoppingList, String listId, String listItemRef, String itemName, HashMap<String, User> sharedWithMap) {
        EditListItemNameDialogFragment editListItemNameDialogFragment = new EditListItemNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_item, listId);
        bundle.putString(Constants.LIST_ITEM_REFERENCE, listItemRef);
        bundle.putString(Constants.KEY_LIST_NAME, itemName);
        bundle.putSerializable(Constants.KEY_SHARED_WITH_MAP, sharedWithMap);
        editListItemNameDialogFragment.setArguments(bundle);

        return editListItemNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListItemRef = getArguments().getString(Constants.LIST_ITEM_REFERENCE);
        mPreviousItemName = getArguments().getString(Constants.KEY_LIST_NAME);
        mSharedWithMap = (HashMap) getArguments().getSerializable(Constants.KEY_SHARED_WITH_MAP);
    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /** {@link EditListDialogFragment#createDialogHelper(int)} is a
         * superclass method that creates the dialog
         */
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);
        helpSetDefaultValueEditText(mPreviousItemName);
        return dialog;
    }

    /**
     * Change selected list item name to the editText input if it is not empty
     */
    protected void doListEdit() {
        String newItemName = mEditTextForList.getText().toString();

        if (!newItemName.equals("") && !newItemName.equals(mPreviousItemName)) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL);
            String itemId = FirebaseDatabase.getInstance().getReferenceFromUrl(mListItemRef).getKey();

            HashMap<String, Object> updatedValues = new HashMap<>();
            Utils.updateMapWithTimestampLastChanged(mListId, mEmail, updatedValues, mSharedWithMap);
            updatedValues.put(Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS+ "/"+mListId+"/"+itemId+"/"+Constants.FIREBASE_PROPERTY_ITEM_NAME, newItemName);

            ref.updateChildren(updatedValues, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Utils.updateTimestampLastChangedReverse(databaseError, mListId, mEmail, mSharedWithMap);
                }
            });
        }
    }
}