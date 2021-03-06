package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Adds a new shopping list
 */
public class AddListDialogFragment extends DialogFragment {
    EditText mEditTextListName;

    /**
     * Public static constructor that creates fragment and
     * passes a bundle with data into it when adapter is created
     */
    public static AddListDialogFragment newInstance() {
        AddListDialogFragment addListDialogFragment = new AddListDialogFragment();
        Bundle bundle = new Bundle();
        addListDialogFragment.setArguments(bundle);
        return addListDialogFragment;
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Open the keyboard automatically when the dialog fragment is opened
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomTheme_Dialog);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_add_list, null);
        mEditTextListName = (EditText) rootView.findViewById(R.id.edit_text_list_name);

        /**
         * Call addShoppingList() when user taps "Done" keyboard action
         */
        mEditTextListName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    addShoppingList();
                }
                return true;
            }
        });

        /* Inflate and set the layout for the dialog */
        /* Pass null as the parent view because its going in the dialog layout*/
        builder.setView(rootView)
                /* Add action buttons */
                .setPositiveButton(R.string.positive_button_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        addShoppingList();
                    }
                });

        return builder.create();
    }

    /**
     * Add new active list
     */
    public void addShoppingList() {

        final String userEnteredName = mEditTextListName.getText().toString();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final String owner = sharedPref.getString(getString(R.string.email_shared_preferences_key), null);

        if (!userEnteredName.equals("")) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            DatabaseReference newListRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.FIREBASE_LOCATION_USERS_LISTS)
                    .child(owner)
                    .push();
            final String listId = newListRef.getKey();

            HashMap<String, Object> updateNewShoppingList = new HashMap<>();

            HashMap <String, Object> timestampCreated = new HashMap<>();
            timestampCreated.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);

            ShoppingList newShoppingList = new ShoppingList(userEnteredName, owner, timestampCreated, listId);

            HashMap<String, Object> shoppingListMap = (HashMap<String, Object>) new ObjectMapper().convertValue(newShoppingList, Map.class);

            Utils.updateMapForAllWithValue(listId, owner, updateNewShoppingList, "", shoppingListMap, null);

            HashMap<String, Object> ownerMappingsUpdate = new HashMap<>();
            ownerMappingsUpdate.put(Constants.FIREBASE_LOCATION_OWNER_MAPPINGS+"/"+listId, owner);

            ref.updateChildren(ownerMappingsUpdate);

//            updateNewShoppingList.put(Constants.FIREBASE_LOCATION_OWNER_MAPPINGS+"/"+listId, owner);
            ref.updateChildren(updateNewShoppingList, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    Utils.updateTimestampLastChangedReverse(databaseError, listId, owner, null);
                }
            });

            AddListDialogFragment.this.getDialog().cancel();
        }
    }

}

