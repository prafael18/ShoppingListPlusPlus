package com.udacity.firebase.shoppinglistplusplus.ui.login;

/**
 * Created by rafael on 04/02/17.
 */


import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

//import com.google.firebase.database.AuthData;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.ui.MainActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static android.R.attr.handle;
import static android.R.attr.name;

/**
 * Represents Sign in screen and functionality of the app
 */
public class LoginActivity extends BaseActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();
    /* A dialog that is presented until the Firebase authentication finished. */
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    private String mEmail, mPassword;
    private User mTemporaryUser;
    private GoogleApiClient mGoogleApiClient;
    private boolean mCreateNewAccount;
    private GoogleSignInAccount mGoogleSignInAccount;

    /**
     * Variables related to Google Login
     */
    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean mGoogleIntentInProgress;
    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;
    /* A Google account object that is populated if the user signs in with Google */
    GoogleSignInAccount mGoogleAccount;
    private String mGoogleEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeScreen();

        mTemporaryUser = new User (null, PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.email_shared_preferences_key), null));

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.email_shared_preferences_key))) {
            String email = intent.getStringExtra(getString(R.string.email_shared_preferences_key));
            mEditTextEmailInput.setText(email);
        }

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    UserInfo userInfo = findProvider(user);
                    if (userInfo != null) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        String email = userInfo.getEmail();
                        String providerId = userInfo.getProviderId();
                        if (providerId.equals(GoogleAuthProvider.PROVIDER_ID)) {
                            setSharedPreferences(encodeEmail(mTemporaryUser.getEmail()), providerId);
                            startActivity(intent);

                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_UID_MAPPINGS);
                            HashMap<String, Object> emailUidMap = new HashMap<>();
                            emailUidMap.put(user.getUid(), encodeEmail(mTemporaryUser.getEmail()));

                            ref.updateChildren(emailUidMap);
                            Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                            finish();
                        }
                        else {
                            if (user.isEmailVerified()) {
                                setSharedPreferences(encodeEmail(mTemporaryUser.getEmail()), providerId);
                                startActivity(intent);
                                Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                                finish();
                            }
                            else {
                                showErrorToast("Email must be verified before logging in.");
                            }
                        }

                    }

                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        mEditTextPasswordInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    signInPassword();
                }

                return true;
            }
        });
    }

    private UserInfo findProvider (FirebaseUser user) {
        for (UserInfo userInfo: user.getProviderData()) {
            String providerId = userInfo.getProviderId();
            String googleProviderId = GoogleAuthProvider.PROVIDER_ID;
            String emailProviderId = EmailAuthProvider.PROVIDER_ID;
            if (userInfo.getProviderId().equals(GoogleAuthProvider.PROVIDER_ID) || userInfo.getProviderId().equals(EmailAuthProvider.PROVIDER_ID)) {
                return userInfo;
            }
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        signInPassword();
    }

    /**
     * Open CreateAccountActivity when user taps on "Sign up" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = (LinearLayout) findViewById(R.id.linear_layout_login_activity);
        initializeBackground(linearLayoutLoginActivity);
        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);
        /* Setup Google Sign In */
        setupGoogleSignIn();
 //       setupGoogleSignIn();
    }

    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() {
        mEmail = mEditTextEmailInput.getText().toString();
        mPassword = mEditTextPasswordInput.getText().toString();

        mTemporaryUser = new User(null, mEmail);

        if (mEmail.equals("")) {
            mEditTextEmailInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }

        if (mPassword.equals("")) {
            mEditTextPasswordInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }

        final AuthCredential credential = EmailAuthProvider.getCredential(mEmail, mPassword);

        mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        mEditTextPasswordInput.setError(e.getMessage());
                    } catch (FirebaseNetworkException e) {
                        showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
                System.out.println("signInWithEmail:onComplete:" + task.isSuccessful());

            }
        });



    }


    private void setSharedPreferences(String email, String providerId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor spe = sp.edit();

        spe.putString(getString(R.string.email_shared_preferences_key), email).apply();
        spe.putString(getString(R.string.provider_shared_preferences_key), providerId).apply();



    }

    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's Google login provider.
     * @param authData AuthData object returned from onAuthenticated
     */

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }


    public void setupGoogleSignIn () {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.OATH_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = (SignInButton) findViewById(R.id.login_with_google);
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_LOGIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    public void handleSignInResult (GoogleSignInResult result) {
        System.out.println("handleSignInResult: " + result.isSuccess());
        if(result.isSuccess()) {
            mAuthProgressDialog.show();
            System.out.println("Successfully logged in using Google Sign In!");
            mGoogleSignInAccount = result.getSignInAccount();
            firebaseAuthWithGoogle(mGoogleSignInAccount);
        }
        else {
            System.out.println("Unsuccessful at using Google Sign In");
        }
    }

    public void firebaseAuthWithGoogle (final GoogleSignInAccount account) {

        System.out.println("firebaseAuthWithGoogle: " + account.getId());

        mTemporaryUser = new User(null, account.getEmail());

        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>(){
            @Override
            public void onComplete (@NonNull Task<AuthResult> task) {
                System.out.println("signInWithCredential: " + task.isSuccessful());
                if (!task.isSuccessful()) {
                    showErrorToast("Authentication Failed");
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        showErrorToast(e.getMessage());
                    } catch (FirebaseAuthInvalidUserException e) {
                        showErrorToast(e.getMessage());
                    } catch (FirebaseAuthUserCollisionException e) {
                        showErrorToast(e.getMessage());
                    } catch (Exception e) {
                        System.out.print(e.getMessage());
                    }
                }
                else {
//                    System.out.println("User: " + firebaseUser.getId() +
//                            " Email: " + firebaseUser.getEmail() +
//                            " Name: " + firebaseUser.getDisplayName() +
//                            " GivenName: " + firebaseUser.getGivenName() +
//                            " FamilyName: " + firebaseUser.getFamilyName());


                    final String email = encodeEmail(account.getEmail());
                    mGoogleEmail = email;

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_LOCATION_USERS);
                    String name = account.getGivenName();
                    User user = new User(name, email);
                    ref = ref.child(email);
                    ref.setValue(user);

//                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.hasChild(email)) {
//                                mCreateNewAccount = false;
//                            }
//                            else {
//                                mCreateNewAccount = true;
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            System.out.println("ERROR: " + databaseError.getMessage());
//                        }
//                    });
//
//                    if (mCreateNewAccount) {
//
//                    }
                }
                mAuthProgressDialog.dismiss();

            }
        });
        System.out.println("We got out of the onCompleteListener for signInWithCredential");
    }

    public static String encodeEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
}