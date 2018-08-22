package com.freedommobile.movemberselfie.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.freedommobile.movemberselfie.MovemberSelfieApplication;
import com.freedommobile.movemberselfie.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * A login screen that offers login via Google sign in.
 */
public class LoginActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {
    public static final String USER_INFO = "USER_INFO";
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mRootView = findViewById(android.R.id.content);
        mAuth = FirebaseAuth.getInstance();
        ImageView movemberLogoImageView = findViewById(R.id.movember_logo);
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(this);

        movemberLogoImageView.setColorFilter(Color.WHITE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    /**
     * Called when a {@link View} has been clicked.
     *
     * @param view The {@link View} that was clicked.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signIntent =
                Auth.GoogleSignInApi
                        .getSignInIntent(MovemberSelfieApplication.mGoogleApiClient);
        startActivityForResult(signIntent, RC_SIGN_IN);
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult signInResult =
                    Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(signInResult);
        }
    }

    private void handleSignInResult(GoogleSignInResult signInResult) {
        Log.d(TAG, "handleSignInResult: " + signInResult.isSuccess());
        if (signInResult.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount signInAccount = signInResult.getSignInAccount();
            if (signInAccount != null) {
                fireBaseAuthWithGoogle(signInAccount);
            }
        } else {
            Snackbar.make(mRootView, "Login failed, please try again later!",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void fireBaseAuthWithGoogle(final GoogleSignInAccount signInAccount) {
        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            Intent signInIntent = new Intent(LoginActivity.this, MainActivity.class);
                            signInIntent.putExtra(USER_INFO, signInAccount);
                            startActivity(signInIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(mRootView, "Authentication Failed.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Intent signInIntent = new Intent(this, MainActivity.class);
            signInIntent.putExtra(USER_INFO, account);
            startActivity(signInIntent);
        }
    }
}
