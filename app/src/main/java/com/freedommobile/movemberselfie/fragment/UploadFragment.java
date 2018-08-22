package com.freedommobile.movemberselfie.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.media.ExifInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.freedommobile.movemberselfie.R;
import com.freedommobile.movemberselfie.activity.MainActivity;
import com.freedommobile.movemberselfie.helper.ImageHelper;
import com.freedommobile.movemberselfie.model.Upload;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.freedommobile.movemberselfie.activity.LoginActivity.USER_INFO;

/**
 * A simple {@link Fragment} subclass.
 */
public class UploadFragment extends BaseFragment implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    public static final int ANGLE_90 = 90;
    public static final int ANGLE_180 = 180;
    public static final int ANGLE_270 = 270;

    private MainActivity mMainActivity;
    private TextView mTextViewProgressBar;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private GoogleSignInAccount mSignInAccount;
    private StorageTask mUploadTask;

    public UploadFragment() {
        // Required empty public constructor
    }

    public static UploadFragment getInstance() {
        return new UploadFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_upload, container, false);

        mMainActivity = (MainActivity) getActivity();
        Button buttonChooseImage = view.findViewById(R.id.button_choose_image);
        Button buttonUpload = view.findViewById(R.id.button_upload);
        TextView textViewShowUploads = view.findViewById(R.id.text_view_show_uploads);
        mTextViewProgressBar = view.findViewById(R.id.progress_bar_text_view);
        mEditTextFileName = view.findViewById(R.id.edit_text_file_name);
        mImageView = view.findViewById(R.id.image_view);
        mProgressBar = view.findViewById(R.id.progress_bar);


        mStorageReference = FirebaseStorage.getInstance().getReference("MovemberSelfieUploads");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("MovemberSelfieUploads");

        buttonChooseImage.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        textViewShowUploads.setOnClickListener(this);

        if (getArguments() != null) {
            mSignInAccount = getArguments().getParcelable(USER_INFO);
        }

        return view;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length > 0) {
                int i;
                String readGranted = null;
                String writeGranted = null;
                for (i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            readGranted = "READ STORAGE GRANTED!";
                        }
                    } else if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            writeGranted = "WRITE STORAGE GRANTED!";
                        }
                    }
                }
                if ("READ STORAGE GRANTED!".equals(readGranted)
                        && "WRITE STORAGE GRANTED!".equals(writeGranted)) {
                    openFileChooser();
                }
            }
        }
    }

    /**
     * Checks if the app has permission to write to the device storage.
     * If the app does not has permission then the user will be prompted to grant permissions.
     *
     * @return {@code true} if permissions are granted, otherwise {@code false}.
     */
    private boolean verifyExternalStoragePermissions() {
        int hasWritePermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadPermission = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        List<String> permissionsNeededList = new ArrayList<>();

        if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeededList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (hasReadPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeededList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!permissionsNeededList.isEmpty()) {
            // We don't have permissions so prompt the user
            requestPermissions(
                    permissionsNeededList.toArray(new String[permissionsNeededList.size()]),
                    REQUEST_EXTERNAL_STORAGE
            );
            return false;
        }
        return true;
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
     * @param data        An Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            String imagePath = ImageHelper.getPath(mContext, mImageUri);

            try {
                Uri downScaleImageAndRotatedIfNecessary =
                        downScaleImageAndRotatedIfNecessary(imagePath);
                if (downScaleImageAndRotatedIfNecessary != null) {
                    mImageUri = downScaleImageAndRotatedIfNecessary;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Glide.with(mContext)
                    .load(mImageUri)
                    .into(mImageView);
        }
    }

    /**
     * Down scales the image and rotate it if it's necessary.
     *
     * @param imagePath The path of the {@link Uri}.
     * @return The {@link Uri} that has been down scaled and rotated if it was necessary.
     * @throws IOException The I/O operations exception that occurred.
     */
    private Uri downScaleImageAndRotatedIfNecessary(String imagePath) throws IOException {
        ExifInterface exifInterface;
        exifInterface = new ExifInterface(imagePath);
        int orientation = Objects.requireNonNull(exifInterface).getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap bitmap = ImageHelper.getScaledBitmapFromUri(mContext, mImageUri);
        Bitmap rotatedBitmap = null;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = ImageHelper.rotateImage(bitmap, ANGLE_90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = ImageHelper.rotateImage(bitmap, ANGLE_180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = ImageHelper.rotateImage(bitmap, ANGLE_270);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                break;
        }

        if (rotatedBitmap != null) {
            return ImageHelper.getImageUri(mContext, rotatedBitmap);
        } else if (bitmap != null) {
            return ImageHelper.getImageUri(mContext, bitmap);
        }
        return null;
    }

    /**
     * Called when a {@link View} has been clicked.
     *
     * @param view The {@link View} that was clicked.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_choose_image:
                if (verifyExternalStoragePermissions()) {
                    openFileChooser();
                }
                break;
            case R.id.button_upload:
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Snackbar.make(view, "Upload in progress",
                            Snackbar.LENGTH_LONG).show();
                } else {
                    uploadFile(view);
                }
                break;
            case R.id.text_view_show_uploads:
                mMainActivity.mNavigation.setSelectedItemId(R.id.navigation_home);
                openHomeFragment();
                break;
        }
    }

    private void openHomeFragment() {
        FragmentTransaction fragmentTransaction =
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, HomeFragment.getInstance());
        fragmentTransaction.commit();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = Objects.requireNonNull(getActivity())
                .getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void uploadFile(final View view) {
        if (mImageUri != null) {
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis()
                    + "." + getFileExtension(mImageUri));
            StorageMetadata storageMetadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .build();
            mUploadTask = fileReference.putFile(mImageUri, storageMetadata)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                    mTextViewProgressBar
                                            .setText(String.format(Locale.US, "%d%%", 0));
                                }
                            }, 2000);

                            if (mSignInAccount != null) {
                                Upload upload =
                                        new Upload(mSignInAccount.getId(),
                                                mEditTextFileName.getText().toString().trim(),
                                                taskSnapshot.getDownloadUrl().toString());

                                String uploadId = mDatabaseReference.push().getKey();
                                if (uploadId != null) {
                                    mDatabaseReference.child(uploadId).setValue(upload);
                                }

                                Snackbar.make(view, "Upload successful",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Snackbar.make(view, exception.getMessage(),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()
                                    / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);
                            mTextViewProgressBar
                                    .setText(String.format(Locale.US, "%d%%", (int) progress));
                        }
                    });
        } else {
            Snackbar.make(view, "No file selected",
                    Snackbar.LENGTH_SHORT).show();
        }
    }
}
