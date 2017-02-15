package com.udacity.firebase.shoppinglistplusplus.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;


/**
 * Created by rafael on 31/01/17.
 */


public class ShoppingList {
    private String listName;
    private String owner;
    private String shoppingListId;
    private HashMap<String, Object> timestampCreated;
    private HashMap<String, Object> timestampLastChanged;
    private HashMap<String, Object> timestampLastChangedReversed;
    private HashMap<String, Object> usersShopping;


    /**
     * Required public constructor
     */
    public ShoppingList() {
    }

    public HashMap<String, Object> getTimestampLastChangedReversed() {
        return timestampLastChangedReversed;
    }

    /**
     * Use this constructor to create new ShoppingLists.
     * Takes shopping list listName and owner. Set's the last
     * changed time to what is stored in ServerValue.TIMESTAMP
     *
     * @param listName
     * @param owner
     */
    public ShoppingList(String listName, String owner, HashMap<String, Object> timestampCreated, String listId) {
        this.listName = listName;
        this.owner = owner;
        this.timestampCreated = timestampCreated;
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
        this.timestampLastChangedReversed = null;

        this.usersShopping = new HashMap<>();
        this.shoppingListId = listId;
    }

    public String getListName() {
        return listName;
    }

    public String getOwner() {
        return owner;
    }

    public HashMap<String, Object> getTimestampCreated() {
        return timestampCreated;
    }
    public HashMap<String, Object> getTimestampLastChanged() {
        return timestampLastChanged;
    }

    public HashMap<String, Object> getUsersShopping() {
        return usersShopping;
    }

    public String getShoppingListId() {
        return shoppingListId;
    }

    @JsonIgnore
    public long getTimestampLastChangedLong() {

        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
    }
//
//    @Exclude
//    public long getTimestampCreatedLong() {
//        return (long) timestampLastChanged.get(Constants.FIREBASE_PROPERTY_TIMESTAMP);
//    }

    @Exclude
    public void updateTimeStampLastChanged() {
        HashMap<String, Object> timestampNowObject = new HashMap<String, Object>();
        timestampNowObject.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampLastChanged = timestampNowObject;
    }



}