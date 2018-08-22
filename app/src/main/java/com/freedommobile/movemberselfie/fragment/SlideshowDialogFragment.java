package com.freedommobile.movemberselfie.fragment;


import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.freedommobile.movemberselfie.R;
import com.freedommobile.movemberselfie.model.Upload;

import java.util.ArrayList;

import static com.freedommobile.movemberselfie.fragment.HomeFragment.IMAGES;
import static com.freedommobile.movemberselfie.fragment.HomeFragment.POSITION;

/**
 * A simple {@link Fragment} subclass.
 */
public class SlideshowDialogFragment extends DialogFragment {
    private ArrayList<Upload> mUploads;
    private ViewPager mViewPager;
    private TextView mPostCount;
    private TextView mPostTitle;
    //  page change listener
    private int mSelectedPosition = 0;
    private Context mContext;

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            displayMetaInfo(position);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {}

        @Override
        public void onPageScrollStateChanged(int arg0) {}
    };

    public SlideshowDialogFragment() {
        // Required empty public constructor
    }

    static SlideshowDialogFragment getInstance() {
        return new SlideshowDialogFragment();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slideshow_dialog, container, false);
        mViewPager = view.findViewById(R.id.view_pager);
        mPostCount = view.findViewById(R.id.fullscreen_post_count);
        mPostTitle = view.findViewById(R.id.title_post);

        if (getArguments() != null) {
            mUploads = (ArrayList<Upload>) getArguments().getSerializable(IMAGES);
            mSelectedPosition = getArguments().getInt(POSITION);
        }

        MyViewPagerAdapter myViewPagerAdapter = new MyViewPagerAdapter();
        mViewPager.setAdapter(myViewPagerAdapter);
        mViewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            private float pointX;
            private float pointY;
            private int tolerance = 50;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        // This is important, if you return TRUE the action of swipe will not take place.
                        return false;
                    case MotionEvent.ACTION_DOWN:
                        pointX = motionEvent.getX();
                        pointY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        boolean sameX = pointX + tolerance > motionEvent.getX()
                                && pointX - tolerance < motionEvent.getX();
                        boolean sameY = pointY + tolerance > motionEvent.getY()
                                && pointY - tolerance < motionEvent.getY();
                        if (sameX && sameY) {
                            if (getActivity() != null) {
                                FragmentManager fragmentManager =
                                        getActivity().getSupportFragmentManager();
                                fragmentManager.popBackStackImmediate();
                            }
                        }
                }
                return false;
            }
        });
        setCurrentItem(mSelectedPosition);

        return view;
    }

    /**
     * Called when a fragment is first attached to its context.
     * {@link #onCreate(Bundle)} will be called after this.
     *
     * @param context The {@link Context}.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }

    /**
     * Called when the fragment is no longer attached to its activity.  This
     * is called after {@link #onDestroy()}.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    private void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position, false);
        displayMetaInfo(mSelectedPosition);
    }

    @SuppressLint("DefaultLocale")
    private void displayMetaInfo(int position) {
        mPostCount.setText(String.format("%s of %d", String.valueOf(position + 1), mUploads.size()));

        Upload upload = mUploads.get(position);
        mPostTitle.setText(upload.getName());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    //  The adapter.
    public class MyViewPagerAdapter extends PagerAdapter {

        private LayoutInflater mLayoutInflater;
        private View mView;

        MyViewPagerAdapter() {
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            if (getActivity() != null) {
                mLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (mLayoutInflater != null) {
                    mView = mLayoutInflater.inflate(R.layout.selfie_fullscreen_preview, container, false);
                    ImageView imageViewPreview = mView.findViewById(R.id.fullscreen_image_preview);
                    Upload upload = mUploads.get(position);

                    Glide.with(mContext).load(upload.getImageUrl())
                            .thumbnail(0.5f)
                            .into(imageViewPreview);

                    container.addView(mView);
                }
            }
            return mView;
        }

        @Override
        public int getCount() {
            return mUploads.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
