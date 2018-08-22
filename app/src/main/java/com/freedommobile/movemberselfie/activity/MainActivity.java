package com.freedommobile.movemberselfie.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.freedommobile.movemberselfie.MovemberSelfieApplication;
import com.freedommobile.movemberselfie.R;
import com.freedommobile.movemberselfie.fragment.CameraFragment;
import com.freedommobile.movemberselfie.fragment.HomeFragment;
import com.freedommobile.movemberselfie.fragment.UploadFragment;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import static com.freedommobile.movemberselfie.activity.LoginActivity.USER_INFO;

public class MainActivity extends AppCompatActivity {
    private HomeFragment mHomeFragment;
    private CameraFragment mCameraFragment;
    private UploadFragment mUploadFragment;
    public BottomNavigationView mNavigation;
    private View mRootView;
    private GoogleSignInAccount mSignInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() != null) {
            mSignInAccount = getIntent().getExtras().getParcelable(USER_INFO);
        }
        mRootView = findViewById(android.R.id.content);
        mHomeFragment = HomeFragment.getInstance();
        mCameraFragment = CameraFragment.getInstance();
        mUploadFragment = UploadFragment.getInstance();

        getUserInfo(mHomeFragment);
        setFragment(mHomeFragment);

        mNavigation = findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        getUserInfo(mHomeFragment);
                        setFragment(mHomeFragment);
                        return true;
                    case R.id.navigation_camera:
                        getUserInfo(mCameraFragment);
                        setFragment(mCameraFragment);
                        return true;
                    case R.id.navigation_upload:
                        getUserInfo(mUploadFragment);
                        setFragment(mUploadFragment);
                        return true;
                }
                return false;
            }
        });
    }

    private void getUserInfo(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(USER_INFO, mSignInAccount);
        fragment.setArguments(bundle);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    public void signOut() {
        Auth.GoogleSignInApi.signOut(MovemberSelfieApplication.mGoogleApiClient)
                .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    Intent signOutIntent =
                            new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(signOutIntent);
                    Snackbar.make(mRootView, "Signed out "
                                    + mSignInAccount.getDisplayName()
                                    + "!",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        MovemberSelfieApplication.mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MovemberSelfieApplication.mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sign_out:
                signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
