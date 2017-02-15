package com.udacity.firebase.shoppinglistplusplus.ui.firebaseui;

/**
 * Created by rafael on 04/02/17.
 */

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

/**
 * This class is a generic way of backing an Android ListView with a Firebase location.
 * It handles all of the child events at the given Firebase location. It marshals received data into the given
 * class type. Extend this class and provide an implementation of <code>populateView</code>, which will be given an
 * instance of your list item mLayout and an instance your class that holds your data. Simply populate the view however
 * you like and this class will handle updating the list as the data changes.
 *
 * <blockquote><pre>
 * {@code
 *     Firebase ref = new Firebase("https://<yourapp>.firebaseio.com");
 *     ListAdapter adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class, android.R.layout.two_line_list_item, mRef)
 *     {
 *         protected void populateView(View view, ChatMessage chatMessage)
 *         {
 *             ((TextView)view.findViewById(android.R.id.text1)).setText(chatMessage.getName());
 *             ((TextView)view.findViewById(android.R.id.text2)).setText(chatMessage.getMessage());
 *         }
 *     };
 *     listView.setListAdapter(adapter);
 * }
 * </pre></blockquote>
 *
 * @param <T> The class type to use as a model for the data contained in the children of the given Firebase location
 */
public abstract class FirebaseListAdapter<T> extends BaseAdapter {

    private final Class<T> mModelClass;
    protected int mLayout;
    protected Activity mActivity;
    FirebaseArray mSnapshots;


    /**
     * @param activity    The activity containing the ListView
     * @param modelClass  Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout This is the layout used to represent a single list item. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of modelClass.
     * @param ref         The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     */
    public FirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, Query ref) {
        mModelClass = modelClass;
        mLayout = modelLayout;
        mActivity = activity;
        mSnapshots = new FirebaseArray(ref);
        /**
         * Attaches a listener that is triggered by FirebaseArray whenever elements are
         * added, removed, moved or changed in the FirebaseArray.
         */
        mSnapshots.setOnChangedListener(new FirebaseArray.OnChangedListener() {
            @Override
            public void onChanged(EventType type, int index, int oldIndex) {
                /**
                 * This listener triggers the BaseAdapter method notifyDataSetChanged.
                 * This notifies views reflecting the data set that they should refresh themselves.
                 */
                notifyDataSetChanged();
            }
        });
    }
    /**
     * @param activity    The activity containing the ListView
     * @param modelClass  Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout This is the layout used to represent a single list item. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of modelClass.
     * @param ref         The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of <code>limit()</code>, <code>startAt()</code>, and <code>endAt()</code>,
     */
    public FirebaseListAdapter(Activity activity, Class<T> modelClass, int modelLayout, DatabaseReference ref) {
        this(activity, modelClass, modelLayout, (Query) ref);
    }

    public void cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mSnapshots.cleanup();
    }

    @Override
    public int getCount() {
        return mSnapshots.getCount();
    }

    @Override
    public T getItem(int i) { return mSnapshots.getItem(i).getValue(mModelClass); }

    public DatabaseReference getRef(int position) { return mSnapshots.getItem(position).getRef(); }

    @Override
    public long getItemId(int i) {
        // http://stackoverflow.com/questions/5100071/whats-the-purpose-of-item-ids-in-android-listview-adapter
        return mSnapshots.getItem(i).getKey().hashCode();
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(mLayout, viewGroup, false);
        }

        T model = mSnapshots.getItem(position).getValue(mModelClass);

        // Call out to subclass to marshall this model into the provided view
        populateView(view, model, position);
        return view;
    }

    /**
     * Each time the data at the given Firebase location changes, this method will be called for each item that needs
     * to be displayed. The first two arguments correspond to the mLayout and mModelClass given to the constructor of
     * this class. The third argument is the item's position in the list.
     * <p>
     * Your implementation should populate the view using the data contained in the model.
     * You should implement either this method or the other {@link FirebaseListAdapter#populateView(View, Object)} method
     * but not both.
     *
     * @param v         The view to populate
     * @param model     The object containing the data used to populate the view
     * @param position  The position in the list of the view being populated
     */
    protected void populateView(View v, T model, int position) {
        populateView(v, model);
    }

    /**
     * This is a backwards compatible version of populateView.
     * <p>
     * You should implement either this method or the other {@link FirebaseListAdapter#populateView(View, Object, int)} method
     * but not both.
     *
     * @see FirebaseListAdapter#populateView(View, Object, int)
     *
     * @param v         The view to populate
     * @param model     The object containing the data used to populate the view
     */
    protected void populateView(View v, T model) {

    }
}