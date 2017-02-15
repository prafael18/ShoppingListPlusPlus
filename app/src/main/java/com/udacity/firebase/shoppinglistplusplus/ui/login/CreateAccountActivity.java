package com.udacity.firebase.shoppinglistplusplus.ui.login;

/**
 * Created by rafael on 04/02/17.
 */


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseError;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.MainActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents Sign up screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private String mUsername, mEmail, mPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mCreateNewAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_UID_MAPPINGS);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (user.isEmailVerified()) {
                        Log.d(LOG_TAG, "onAuthStateChanged:signed_in and verified email:" + user.getUid());
                        Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    else {
                        Log.d(LOG_TAG, "onAuthStateChanged:signed_in and email not verified:" + user.getUid());
                        HashMap<String, Object> emailUidMap = new HashMap<>();
                        emailUidMap.put(user.getUid(), LoginActivity.encodeEmail(user.getEmail()));
                        ref.updateChildren(emailUidMap);
                        Intent intent = new Intent (getApplicationContext(), LoginActivity.class);
                        intent.putExtra(getString(R.string.email_shared_preferences_key), mEmail);
                        startActivity(intent);
                    }

                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
    }

    @Override
    public void onStart () {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop () {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }


    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);
    }

    /**
     * Open LoginActivity when user taps on "Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {

        mUsername = mEditTextUsernameCreate.getText().toString();
        mEmail = mEditTextEmailCreate.getText().toString();
        mPassword = mEditTextPasswordCreate.getText().toString();

        boolean validUsername = isUserNameValid(mUsername);
        boolean validEmail = isEmailValid(mEmail);
        boolean validPassword = isPasswordValid(mPassword);

        if (!validUsername || !validEmail || !validPassword) return;

        mAuthProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                final FirebaseUser user = task.getResult().getUser();
                if(task.isSuccessful()) {
                    String encodedEmail = LoginActivity.encodeEmail(mEmail);
                    User newUser = new User (mUsername, encodedEmail);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);
                    ref.child(encodedEmail).setValue(newUser);
                }
                else {
                    try{
                        throw task.getException();
//                    } catch (FirebaseAuthUserCollisionException e) {
//                        mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                    } catch (FirebaseAuthWeakPasswordException e) {
                        mEditTextPasswordCreate.setError(e.getReason());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                mAuthProgressDialog.dismiss();

            }
        });
    }

    /**
     * Creates a new user in Firebase from the Java POJO
     */
    private void createUserInFirebaseHelper(final String encodedEmail) {
    }

    private boolean isEmailValid(String email) {
        boolean emailValid = email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        if (emailValid) {
            return true;
        }
        else {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid), email));
            return false;
        }
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")) {
            mEditTextUsernameCreate.setError(getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password){
        if (password.length() > 5) {
            return true;
        }
        else {
            mEditTextPasswordCreate.setError(getString(R.string.error_invalid_password_not_valid));
            return false;
        }
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}