package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

/**
 * Created by rafael on 31/01/17.
 */

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.key;

/**
 * Lets the user remove active shopping list
 */
public class RemoveListDialogFragment extends DialogFragment {
    final static String LOG_TAG = RemoveListDialogFragment.class.getSimpleName();
    private String mListId;
    private String mEmail;
    private HashMap<String, User> mSharedWithMap;
    /**
     * Public static constructor that creates fragment and passes a bundle with data into it when adapter is created
     */
    public static RemoveListDialogFragment newInstance(ShoppingList shoppingList, String listId, HashMap<String, User> sharedWithMap) {
        RemoveListDialogFragment removeListDialogFragment = new RemoveListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PUSH_ID_KEY, listId);
        bundle.putSerializable(Constants.KEY_SHARED_WITH_MAP, sharedWithMap);
        removeListDialogFragment.setArguments(bundle);
        return removeListDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mListId = bundle.getString(Constants.PUSH_ID_KEY);
        mEmail = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.email_shared_preferences_key), null);
        mSharedWithMap = (HashMap) bundle.getSerializable(Constants.KEY_SHARED_WITH_MAP);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog)
                .setTitle(getActivity().getResources().getString(R.string.action_remove_list))
                .setMessage(getString(R.string.dialog_message_are_you_sure_remove_list))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeList();
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* Dismiss the dialog */
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert);

        return builder.create();
    }

    private void removeList() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> removeList = new HashMap<>();
        Utils.updateMapForAllWithValue(mListId, mEmail, removeList, "", null, mSharedWithMap);
        removeList.put(Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS+"/"+mListId, null);
        removeList.put(Constants.FIREBASE_LOCATION_SHARED_WITH+"/"+mListId, null);

        ref.updateChildren(removeList, new DatabaseReference.CompletionListener(){
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebaseError != null) {
                    Log.e (LOG_TAG, getString(R.string.log_error_updating_data) + firebaseError.getMessage());
                }
            }
        });
    }

}