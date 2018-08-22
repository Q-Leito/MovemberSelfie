package com.freedommobile.movemberselfie.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.freedommobile.movemberselfie.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends BaseFragment {
    private static final String TAG = "CameraFragment";
    private static final int REQUEST_PERMISSIONS = 1234;
    public static String CAMERA_POSITION_BACK;

    private View mRootView;
    private boolean mPermissions;
    public String mCameraOrientation = "none";

    public CameraFragment() {
        // Required empty public constructor
    }

    public static CameraFragment getInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_camera, container, false);

        init();

        return mRootView;
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (mPermissions) {
                init();
            } else {
                verifyPermissions();
            }
        }
    }

    private void init() {
        if (mPermissions) {
            if (checkCameraHardware()) {
                // Open the camera.
                startCamera2();
            } else {
                // Show info.
                Snackbar.make(mRootView,
                        "You need a front-facing camera to use this application",
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            verifyPermissions();
        }
    }

    private void startCamera2() {

    }

    /**
     * Check if this device has a camera.
     *
     * @return {@code true} if this device has a front-facing camera, otherwise {@code false}.
     */
    private boolean checkCameraHardware() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
    }

    /**
     * Checks if the app has permission to write to the device storage.
     * If the app does not has permission then the user will be prompted to grant permissions.
     */
    public void verifyPermissions() {
        String[] permissionsToVerify = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};

        if (ContextCompat.checkSelfPermission(mContext, permissionsToVerify[0])
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, permissionsToVerify[1])
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(mContext, permissionsToVerify[2])
                == PackageManager.PERMISSION_GRANTED) {
            mPermissions = true;
            init();
        } else {
            // We don't have permissions so prompt the user
            requestPermissions(permissionsToVerify, REQUEST_PERMISSIONS);
        }
    }
}
