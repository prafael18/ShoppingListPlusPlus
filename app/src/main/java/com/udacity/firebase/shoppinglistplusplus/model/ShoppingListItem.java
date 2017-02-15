package com.udacity.firebase.shoppinglistplusplus.model;

/**
 * Created by rafael on 03/02/17.
 */

public class ShoppingListItem {
    private String itemName;
    private String owner;
    private boolean bought;
    private String buyerEmail;

    public ShoppingListItem() {
    }

    public ShoppingListItem(String itemName, String owner) {
        this.itemName = itemName;
        this.owner = owner;
        this.bought = false;
    }


    public String getItemName() {
        return itemName;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isBought() {
        return bought;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyer (String buyerEmail, boolean bought) {
        this.buyerEmail = buyerEmail;
        this.bought = bought;
    }
}
