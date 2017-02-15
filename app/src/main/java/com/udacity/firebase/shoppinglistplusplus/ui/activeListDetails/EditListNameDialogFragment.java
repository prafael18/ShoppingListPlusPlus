package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

/**
 * Created by rafael on 31/01/17.
 */
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

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
 * Lets user edit the list name for all copies of the current list
 */
public class EditListNameDialogFragment extends EditListDialogFragment {
    private static final String LOG_TAG = ActiveListDetailsActivity.class.getSimpleName();
    String mPreviousListName;
    private HashMap<String, User> mSharedWithMap;

    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static EditListNameDialogFragment newInstance(ShoppingList shoppingList, String listId, HashMap<String, User> sharedWithMap) {
        EditListNameDialogFragment editListNameDialogFragment = new EditListNameDialogFragment();
        Bundle bundle = EditListDialogFragment.newInstanceHelper(shoppingList, R.layout.dialog_edit_list, listId);
        bundle.putString(Constants.KEY_LIST_NAME, shoppingList.getListName());
        bundle.putSerializable(Constants.KEY_SHARED_WITH_MAP, sharedWithMap);
        editListNameDialogFragment.setArguments(bundle);
        return editListNameDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mPreviousListName = bundle.getString(Constants.KEY_LIST_NAME);
        mSharedWithMap = (HashMap) bundle.getSerializable(Constants.KEY_SHARED_WITH_MAP);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.createDialogHelper(R.string.positive_button_edit_item);

        helpSetDefaultValueEditText(mPreviousListName);
        return dialog;
    }

    /**
     * Changes the list name in all copies of the current list
     */
    protected void doListEdit() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> updateListName = new HashMap<>();
        Utils.updateMapForAllWithValue(mListId, mEmail, updateListName, Constants.FIREBASE_PROPERTY_LIST_NAME, mEditTextForList.getText().toString(), mSharedWithMap);
        Utils.updateMapWithTimestampLastChanged(mListId, mEmail, updateListName, mSharedWithMap);
        ref.updateChildren(updateListName, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Utils.updateTimestampLastChangedReverse(databaseError, mListId, mEmail, mSharedWithMap);
            }
        });
    }
}