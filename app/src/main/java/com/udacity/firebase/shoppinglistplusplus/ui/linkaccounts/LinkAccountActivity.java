package com.udacity.firebase.shoppinglistplusplus.ui.linkaccounts;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.udacity.firebase.shoppinglistplusplus.R;
import com.udacity.firebase.shoppinglistplusplus.model.User;
import com.udacity.firebase.shoppinglistplusplus.ui.BaseActivity;
import com.udacity.firebase.shoppinglistplusplus.utils.Constants;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by rafael on 05/02/17.
 */

public class LinkAccountActivity extends BaseActivity {
    private FirebaseAuth mAuth;
    private User user;
    private Button mGoogleButton;
    private GoogleApiClient mGoogleApiClient;
    private int RC_SIGN_IN_GOOGLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_account);


        // GOOGLE SIGN IN
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.OATH_CLIENT_ID)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        initializeScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initializeScreen () {
        mGoogleButton = (Button) findViewById(R.id.link_with_google_button);
        if (isALinkedProvider (GoogleAuthProvider.PROVIDER_ID)) {
            mGoogleButton.setText("Unlink from Google");
        }
        else {
            mGoogleButton.setText("Link to Google");
        }
    }

    private boolean isALinkedProvider (String providerId) {
        for (UserInfo userInfo: mAuth.getCurrentUser().getProviderData()) {
            String provider = userInfo.getProviderId();
            if (provider.equals(providerId)) {
                return true;
            }
        }
        return false;
    }

    public void linkWithGoogleAccount (View v) {
        if (isALinkedProvider(GoogleAuthProvider.PROVIDER_ID)){
            unlinkProvider(GoogleAuthProvider.PROVIDER_ID);
            return;
        }

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN_GOOGLE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else {
                System.out.println("Unsucessful at getting signInResult from Google!");
            }
        }
    }


    public void firebaseAuthWithGoogle (GoogleSignInAccount account) {
        String accessToken = account.getIdToken();
        AuthCredential credential = GoogleAuthProvider.getCredential(accessToken, null);
        FirebaseUser user = mAuth.getCurrentUser();
        user.linkWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                initializeScreen();
                System.out.println("linkWithCredential : " + task.isSuccessful());
            }
        });
    }

    private void unlinkProvider(final String providerId ){

        mAuth.getCurrentUser().unlink( providerId )
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        initializeScreen();
                        System.out.println("unlink from provider " + providerId + " was: " + task.isSuccessful());
                    }
                });
    }
}
