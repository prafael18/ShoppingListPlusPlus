package com.udacity.firebase.shoppinglistplusplus.ui.sharing;

/**
 * Created by rafael on 07/02/17.
 */

import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Query;
import com.udacity.firebase.shoppinglistplusplus.ui.firebaseui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;


/**
 * Populates the list_view_friends_autocomplete inside AddFriendActivity
 */
public class AutocompleteFriendAdapter extends FirebaseListAdapter<User> {

    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public AutocompleteFriendAdapter(Activity activity, Class<User> modelClass, int modelLayout,
                                     Query ref) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_autocomplete_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, final User user) {
        TextView userEmail = (TextView) view.findViewById(R.id.text_view_autocomplete_item);
        userEmail.setText(Utils.decodeEmail(user.getEmail()));
    }

    /** Checks if the friend you try to add is the current user **/
    public boolean isNotCurrentUser(User user, String userLoggedInEmail) {
        if (!user.getEmail().equals(userLoggedInEmail)) {
            return true;
        }
        return false;
    }

    /** Checks if the friend you try to add is already added, given a dataSnapshot of a user **/
    public boolean isNotAlreadyAdded(DataSnapshot dataSnapshot, User user) {
//        for(DataSnapshot oneUserSnapshot: dataSnapshot.getChildren()) {
//            User currentIteratingUser = oneUserSnapshot.getValue(User.class);
//            if(currentIteratingUser.getEmail().equals(user.getEmail())) {
//                return false;
//            }
//        }

        if (dataSnapshot.getValue(User.class) != null) {
            return false;
        }
        return true;
    }

}
