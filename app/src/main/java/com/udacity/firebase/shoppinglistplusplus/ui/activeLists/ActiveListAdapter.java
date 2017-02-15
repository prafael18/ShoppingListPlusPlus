package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;

/**
 * Created by rafael on 02/02/17.
 */


import android.app.Activity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.firebaseui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Populates the list_view_active_lists inside ShoppingListsFragment
 */
public class ActiveListAdapter extends FirebaseListAdapter<ShoppingList> {
    private String mEmailCurrentUser;

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ActiveListAdapter(Activity activity, Class<ShoppingList> modelClass, int modelLayout,
                             Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_active_lists)
     * with items inflated from single_active_list.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, ShoppingList list) {

        TextView listname = (TextView) view.findViewById(R.id.text_view_list_name);
        final TextView owner = (TextView) view.findViewById(R.id.text_view_created_by_user);
        final TextView userCount = (TextView) view.findViewById(R.id.text_view_users_shopping_count);

        mEmailCurrentUser = PreferenceManager.getDefaultSharedPreferences(mActivity).getString(mActivity.getString(R.string.email_shared_preferences_key), null);

        listname.setText(list.getListName());

        //Make sure that "usersShopping" exists before getting a reference to it
        HashMap<String, Object> usersShopping = list.getUsersShopping();
        if (usersShopping != null) {
            if (usersShopping.size() == 1) {
                userCount.setText(mActivity.getString(R.string.list_one_person_shopping));
            }
            else {
                userCount.setText(String.format(mActivity.getString(R.string.list_people_shopping), usersShopping.size()));
            }
        }
        else {
            userCount.setText("");
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);


        final String emailListOwner = list.getOwner();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.child(mEmailCurrentUser).getValue(User.class);
                String userNameCurrentUser = user.getName();
                user = dataSnapshot.child(emailListOwner).getValue(User.class);
                String userNameListOwner = user.getName();

                if (userNameCurrentUser.equals(userNameListOwner)) {
                    owner.setText(mActivity.getString(R.string.you_are_the_owner));
                }
                else {
                    owner.setText(userNameListOwner);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Adding a new list failed: " + databaseError.getMessage());
            }
        });

    }
}