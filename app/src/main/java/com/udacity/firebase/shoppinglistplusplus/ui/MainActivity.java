package com.udacity.firebase.shoppinglistplusplus.ui;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.activeLists.AddListDialogFragment;
import com.udacity.firebase.shoppinglistplusplus.ui.activeLists.ShoppingListsFragment;
import com.udacity.firebase.shoppinglistplusplus.ui.linkaccounts.LinkAccountActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.login.LoginActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.meals.AddMealDialogFragment;
import com.udacity.firebase.shoppinglistplusplus.ui.meals.MealsFragment;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

/**
 * Represents the home screen of the app which
 * has a {@link ViewPager} with {@link ShoppingListsFragment} and {@link MealsFragment}
 */
public class MainActivity extends BaseActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private String encodedEmail, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preference_screen, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        encodedEmail = sharedPref.getString(getString(R.string.email_shared_preferences_key), null);
        String providerId = sharedPref.getString(getString(R.string.provider_shared_preferences_key), null);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(encodedEmail)) {
                    String userName = dataSnapshot.child(encodedEmail).child(Constants.FIREBASE_PROPERTY_USERNAME).getValue().toString();
                    setTitle(userName +"\'s Lists");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        /**
         * Link layout elements from XML and setup the toolbar
         */
        initializeScreen();
        //TestFirebase.onChildAddedTest();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Override onOptionsItemSelected to use main_menu instead of BaseActivity menu
     *
     * @param menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Override onOptionsItemSelected to add action_settings only to the MainActivity
     *
     * @param item
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.link_accounts) {
            Intent intent = new Intent (this, com.udacity.firebase.shoppinglistplusplus.ui.linkaccounts.LinkAccountActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_logout) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        if (id == R.id.action_sort) {
            startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Link layout elements from XML and setup the toolbar
     */
    public void initializeScreen() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        /**
         * Create SectionPagerAdapter, set it as adapter to viewPager with setOffscreenPageLimit(2)
         **/
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        /**
         * Setup the mTabLayout with view pager
         */
        tabLayout.setupWithViewPager(viewPager);
    }

    /**
     * Create an instance of the AddList dialog fragment and show it
     */
    public void showAddListDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddListDialogFragment.newInstance();
        dialog.show(MainActivity.this.getFragmentManager(), "AddListDialogFragment");
    }

    /**
     * Create an instance of the AddMeal dialog fragment and show it
     */
    public void showAddMealDialog(View view) {
        /* Create an instance of the dialog fragment and show it */
        DialogFragment dialog = AddMealDialogFragment.newInstance();
        dialog.show(MainActivity.this.getFragmentManager(), "AddMealDialogFragment");
    }

    /**
     * SectionPagerAdapter class that extends FragmentStatePagerAdapter to save fragments state
     */
    public class SectionPagerAdapter extends FragmentStatePagerAdapter {

        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Use positions (0 and 1) to find and instantiate fragments with newInstance()
         *
         * @param position
         */
        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;

            /**
             * Set fragment to different fragments depending on position in ViewPager
             */
            switch (position) {
                case 0:
                    fragment = ShoppingListsFragment.newInstance();
                    break;
                case 1:
                    fragment = MealsFragment.newInstance();
                    break;
                default:
                    fragment = ShoppingListsFragment.newInstance();
                    break;
            }

            return fragment;
        }


        @Override
        public int getCount() {
            return 2;
        }

        /**
         * Set string resources as titles for each fragment by it's position
         *
         * @param position
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.pager_title_shopping_lists);
                case 1:
                default:
                    return getString(R.string.pager_title_meals);
            }
        }
    }
}
