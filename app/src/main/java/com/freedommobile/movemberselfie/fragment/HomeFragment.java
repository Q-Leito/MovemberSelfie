package com.freedommobile.movemberselfie.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.freedommobile.movemberselfie.R;
import com.freedommobile.movemberselfie.adapter.SelfieGalleryAdapter;
import com.freedommobile.movemberselfie.model.Upload;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;

import static com.freedommobile.movemberselfie.activity.LoginActivity.USER_INFO;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends BaseFragment {
    public static final String IMAGES = "IMAGES";
    public static final String POSITION = "POSITION";
    public static final String FIND_THIS_FRAGMENT_TAG = "findThisFragment";
    private SelfieGalleryAdapter mSelfieGalleryAdapter;

    private ProgressBar mProgressCircle;

    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseReference;
    private ValueEventListener mValueEventListener;
    private GoogleSignInAccount mSignInAccount;
    private ArrayList<Upload> mUploads;
    private TextView mEmptyTextView;
    private TextView mProfilePostCount;
    private TextView mProfilePostText;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment getInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(
                StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mProgressCircle = view.findViewById(R.id.progress_circle);
        mEmptyTextView = view.findViewById(R.id.no_selfie_yet_text);
        ImageView profileImage = view.findViewById(R.id.profile_image);
        TextView profileName = view.findViewById(R.id.profile_name);
        TextView profileEmail = view.findViewById(R.id.profile_email);
        mProfilePostCount = view.findViewById(R.id.profile_post_count);
        mProfilePostText = view.findViewById(R.id.profile_post_text);

        if (getArguments() != null) {
            mSignInAccount = getArguments().getParcelable(USER_INFO);
        }

        if (mSignInAccount != null) {
            mSignInAccount.getId();
            profileName.setText(mSignInAccount.getDisplayName());
            profileEmail.setText(mSignInAccount.getEmail());

            Glide.with(mContext)
                    .load(mSignInAccount.getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profileImage);
        }

        mStorage = FirebaseStorage.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("MovemberSelfieUploads");

        mUploads = new ArrayList<>();
        mSelfieGalleryAdapter = new SelfieGalleryAdapter(mContext, mUploads);
        recyclerView.setAdapter(mSelfieGalleryAdapter);
        mSelfieGalleryAdapter.setOnItemClickListener(new SelfieGalleryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(IMAGES, mUploads);
                bundle.putInt(POSITION, position);

                SlideshowDialogFragment slideshowDialogFragment = SlideshowDialogFragment.getInstance();
                slideshowDialogFragment.setArguments(bundle);
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.main_frame, slideshowDialogFragment, FIND_THIS_FRAGMENT_TAG)
                            .addToBackStack(null)
                            .commit();
                }
            }

            @Override
            public void onLongItemClick(final int position) {
                Upload selectedItem = mUploads.get(position);
                final String selectedKey = selectedItem.getImageKey();

                StorageReference imageReference = mStorage
                        .getReferenceFromUrl(selectedItem.getImageUrl());
                imageReference.delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDatabaseReference.child(selectedKey).removeValue();
                                Snackbar.make(view, "Item deleted",
                                        Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Snackbar.make(view, exception.getMessage(),
                                        Snackbar.LENGTH_LONG).show();
                            }
                        });
            }
        });

        mValueEventListener = mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUploads.clear();
                if (dataSnapshot.exists()) {
                    mEmptyTextView.setVisibility(View.GONE);
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Upload upload = postSnapshot.getValue(Upload.class);
                        if (upload != null) {
                            upload.setImageKey(postSnapshot.getKey());
                            if (upload.getId().equals(mSignInAccount.getId())) {
                                mUploads.add(upload);
                            }
                        }
                    }
                    mProgressCircle.setVisibility(View.GONE);
                    mProfilePostCount.setText(String.valueOf(mUploads.size()));
                    mProfilePostText.setText(R.string.posts);
                    Collections.reverse(mUploads);
                    mSelfieGalleryAdapter.notifyDataSetChanged();
                } else {
                    mProgressCircle.setVisibility(View.GONE);
                    mEmptyTextView.setText(Html.fromHtml(getString(R.string.empty_gallery_label)));
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Snackbar.make(view, databaseError.getMessage(),
                        Snackbar.LENGTH_LONG).show();
                mProgressCircle.setVisibility(View.GONE);
            }
        });

        return view;
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDatabaseReference.removeEventListener(mValueEventListener);
    }
}
