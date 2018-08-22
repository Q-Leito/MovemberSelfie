package com.freedommobile.movemberselfie.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Upload implements Serializable {
    private String mId;
    private String mName;
    private String mImageUrl;
    private String mImageKey;

    public Upload() {
        // Empty constructor needed.
    }

    public Upload(String id, String name, String imageUrl) {
        mId = id;
        mName = name;
        mImageUrl = imageUrl;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    @Exclude
    public String getImageKey() {
        return mImageKey;
    }

    @Exclude
    public void setImageKey(String imageKey) {
        mImageKey = imageKey;
    }
}
