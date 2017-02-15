package com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails;

/**
 * Created by rafael on 03/02/17.
 */

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.firebaseui.FirebaseListAdapter;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingListItem;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import org.w3c.dom.Text;

import java.util.HashMap;


/**
 * Populates list_view_shopping_list_items inside ActiveListDetailsActivity
 */
public class ActiveListItemAdapter extends FirebaseListAdapter<ShoppingListItem> {
    private String mListId, mUserEmail;
    private ShoppingList mShoppingList;
    private HashMap<String, User> mSharedWithMap;
    /**
     * Public constructor that initializes private instance variables when adapter is created
     */
    public ActiveListItemAdapter(Activity activity, Class<ShoppingListItem> modelClass, int modelLayout,
                                 Query ref, String listId, String buyerEmail) {
        super(activity, modelClass, modelLayout, ref);
        this.mActivity = activity;
        this.mListId = listId;
        this.mUserEmail = buyerEmail;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.mShoppingList = shoppingList;
        this.notifyDataSetChanged();
    }

    public void setSharedWithMap (HashMap<String, User> sharedWithMap) {
        mSharedWithMap = sharedWithMap;
        this.notifyDataSetChanged();
    }

    /**
     * Protected method that populates the view attached to the adapter (list_view_friends_autocomplete)
     * with items inflated from single_active_list_item.xml
     * populateView also handles data changes and updates the listView accordingly
     */
    @Override
    protected void populateView(View view, final ShoppingListItem item, final int position) {
        TextView itemName = (TextView) view.findViewById(R.id.text_view_active_list_item_name);
//        itemName.setText(item.getItemName());
        ImageView removeIcon = (ImageView) view.findViewById(R.id.button_remove_item);
        TextView boughtBy = (TextView) view.findViewById(R.id.text_view_bought_by);
        TextView buyer = (TextView) view.findViewById(R.id.text_view_bought_by_user);

        if (item.isBought()) {
            itemName.setText(item.getItemName());

            removeIcon.setVisibility(View.INVISIBLE);
            int getPaintFlags = itemName.getPaintFlags();
            int paintflag = Paint.STRIKE_THRU_TEXT_FLAG;
            itemName.setPaintFlags(itemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            boughtBy.setVisibility(View.VISIBLE);
            buyer.setVisibility(View.VISIBLE);
            if (item.getBuyerEmail().equals(mUserEmail)) {
                buyer.setText("You");
            }
            else {
                buyer.setText(getItem(position).getBuyerEmail());
            }
        }
        else {
            itemName.setText(item.getItemName());

            int getPaintFlags = itemName.getPaintFlags();
            int paintflag = Paint.STRIKE_THRU_TEXT_FLAG;

            itemName.setPaintFlags(itemName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            boughtBy.setVisibility(View.INVISIBLE);
            buyer.setVisibility(View.INVISIBLE);

            if (item.getOwner().equals(mUserEmail) || mShoppingList.getOwner().equals(mUserEmail)) {
                removeIcon.setVisibility(View.VISIBLE);
                removeIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity, R.style.CustomTheme_Dialog)
                                .setTitle(mActivity.getString(R.string.remove_item_option))
                                .setMessage(mActivity.getString(R.string.dialog_message_are_you_sure_remove_item))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        removeItem(position);
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

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }
                });
            }
            else {
                removeIcon.setVisibility(View.INVISIBLE);
            }

        }
    }

    private void removeItem(int position) {
        final String email = PreferenceManager.getDefaultSharedPreferences(mActivity).getString(mActivity.getString(R.string.email_shared_preferences_key), null);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL);
        DatabaseReference itemRef = this.getRef(position);
        String item = itemRef.getKey();

        HashMap<String, Object> updatedValues = new HashMap<>();
        Utils.updateMapWithTimestampLastChanged(mListId, email, updatedValues, mSharedWithMap);
        updatedValues.put (Constants.FIREBASE_LOCATION_SHOPPING_LIST_ITEMS+"/"+mListId+"/"+item, null);
        ref.updateChildren(updatedValues, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Utils.updateTimestampLastChangedReverse(databaseError, mListId, email, mSharedWithMap);
            }
        });
    }
}

