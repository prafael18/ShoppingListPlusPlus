package com.udacity.firebase.shoppinglistplusplus.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.BuildConfig;

import java.util.HashMap;

import static android.R.id.list;

/**
 * Constants class store most important strings and paths of the app
 */
public final class Constants {

    /**
     * Constants related to locations in Firebase, such as the name of the node
     * where active lists are stored (ie "activeLists")
     */

    public static final String FIREBASE_LOCATION_USERS = "users";
    public static final String FIREBASE_LOCATION_SHOPPING_LIST_ITEMS = "shoppingListItems";
    public static final String FIREBASE_LOCATION_USERS_LISTS = "userLists";
    public static final String FIREBASE_LOCATION_USERS_FRIENDS = "userFriends";
    public static final String FIREBASE_LOCATION_SHARED_WITH = "sharedWith";
    public static final String FIREBASE_URL_SHOPPING_LIST_ITEMS = BuildConfig.UNIQUE_FIREBASE_ROOT_URL+"/"+FIREBASE_LOCATION_SHOPPING_LIST_ITEMS;
    public static final String FIREBASE_LOCATION_UID_MAPPINGS = "uidMappings";
    public static final String FIREBASE_LOCATION_OWNER_MAPPINGS = "listOwnerMappings";

    /**
     * Constants for Firebase object properties
     */
    public static final String FIREBASE_PROPERTY_LIST_NAME = "listName";
    public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED = "timestampLastChanged";
    public static final String FIREBASE_PROPERTY_ITEM_NAME = "itemName";
    public static final String FIREBASE_PROPERTY_USERNAME = "name";
    public static final String FIREBASE_PROPERTY_USERS_SHOPPING= "usersShopping";
    public static final String FIREBASE_PROPERTY_USERS_OWNER ="owner";
    public static final String FIREBASE_PROPERTY_BOUGHT = "bought";
    public static final String FIREBASE_PROPERTY_EMAIL = "email";
    public static final String FIREBASE_PROPERTY_TIMESTAMP_REVERSED = "timestampLastChangedReversed";
    /**
     * Constants for Firebase URL
     */
    public static final String FIREBASE_URL = BuildConfig.UNIQUE_FIREBASE_ROOT_URL;


    /**
     * Constants for bundles, extras and shared preferences keys
     */
    public static final String KEY_LAYOUT_RESOURCE = "LAYOUT_RESOURCE";
    public static final String PUSH_ID_KEY = "PUSH_ID";
    public static final String LIST_ITEM_REFERENCE = "LIST_ITEM_KEY";
    public static final String KEY_SHARED_WITH_MAP = "SHARED_WITH_MAP";

   // public static final String FIREBASE_PROPERTY_TIMESTAMP_LAST_CHANGED;
    public static final String KEY_LIST_NAME = "LISTNAME_RESOURCE";


    public static final String OATH_CLIENT_ID = "795432961302-i19f0bu76m77hvi2m2n20r15caeavk57.apps.googleusercontent.com";

    public static final String KEY_PREF_SORT_ORDER_LISTS = "PERF_SORT_ORDER_LISTS";

    public static final String ORDER_BY_KEY = "orderByPushKey";
    public static final String ORDER_BY_OWNER_EMAIL = "orderByOwnerEmail";
    public static final String ORDER_BY_TIMESTAMP_REVERSED = "timestampLastChangedReversed";


}
