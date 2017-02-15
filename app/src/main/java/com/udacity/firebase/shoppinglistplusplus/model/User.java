package com.udacity.firebase.shoppinglistplusplus.model;

import com.google.firebase.database.ServerValue;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;

/**
 * Created by rafael on 05/02/17.
 */

public class User {
    private String name, email;
    private HashMap<String, Object> timestampJoined;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        HashMap<String, Object> timestampJoined = new HashMap<>();
        timestampJoined.put (Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
        this.timestampJoined = timestampJoined;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public HashMap<String, Object> getTimestampJoined() {
        return timestampJoined;
    }
}
