package com.udacity.firebase.shoppinglistplusplus.ui.activeLists;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.ShoppingList;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.MainActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.activeListDetails.ActiveListDetailsActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;
import com.udacity.firebase.shoppinglistplusplus.utils.Utils;

import java.util.Date;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import static android.R.attr.data;
import static android.R.attr.onClick;
import static android.R.attr.start;
import static java.sql.Types.REF;


/**
 * A simple {@link Fragment} subclass that shows a list of all shopping lists a user can see.
 * Use the {@link ShoppingListsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShoppingListsFragment extends Fragment {
    private ListView mListView;
    private ActiveListAdapter mAdapter;
    private Query mQuery;
    private String mEmailCurrentUser;
    SharedPreferences.OnSharedPreferenceChangeListener mListener;

    public ShoppingListsFragment() {
        /* Required empty public constructor */
    }

    /**
     * Create fragment and pass bundle with data as it's arguments
     * Right now there are not arguments...but eventually there will be.
     */
    public static ShoppingListsFragment newInstance() {
        ShoppingListsFragment fragment = new ShoppingListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(Constants.KEY_PREF_SORT_ORDER_LISTS, Constants.ORDER_BY_KEY);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Constants.FIREBASE_LOCATION_USERS_LISTS)
                .child(mEmailCurrentUser);

//        ref.keepSynced(true);

        Query query;

        if (sortOrder.equals(Constants.ORDER_BY_KEY)) {
            query = ref.orderByKey();
        } else if (sortOrder.equals(Constants.ORDER_BY_TIMESTAMP_REVERSED)) {
            query = ref.orderByChild(sortOrder+"/"+Constants.FIREBASE_PROPERTY_TIMESTAMP);
        }
        else {
            query = ref.orderByChild(sortOrder);
        }

        mAdapter = new ActiveListAdapter(getActivity(), ShoppingList.class, R.layout.single_active_list, query);
        mListView.setAdapter(mAdapter);
    }


    @Override
    public void onPause() {
        super.onPause();
        mAdapter.cleanup();
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Initialize instance variables with data from bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        mEmailCurrentUser = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.email_shared_preferences_key), null);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_shopping_lists, container, false);
        initializeScreen(rootView);

        /**
         * Set interactive bits, such as click events and adapters
         */
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ActiveListDetailsActivity.class);
                DatabaseReference ref = mAdapter.getRef(position);
                String key = ref.getKey();
                intent.putExtra(Constants.PUSH_ID_KEY, key);
                startActivity(intent);
            }
        });

        return rootView;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * Link layout elements from XML
     */
    private void initializeScreen(View rootView) {
        mListView = (ListView) rootView.findViewById(R.id.list_view_active_lists);
    }
}
