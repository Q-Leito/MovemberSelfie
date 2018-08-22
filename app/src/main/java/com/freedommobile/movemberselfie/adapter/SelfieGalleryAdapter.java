package com.freedommobile.movemberselfie.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.freedommobile.movemberselfie.R;
import com.freedommobile.movemberselfie.model.Upload;

import java.util.List;

public class SelfieGalleryAdapter extends RecyclerView.Adapter<SelfieGalleryAdapter.ViewHolder> {
    private Context mContext;
    private List<Upload> mUploads;
    private OnItemClickListener mOnItemClickListener;

    public SelfieGalleryAdapter(Context context, List<Upload> uploads) {
        mContext = context;
        mUploads = uploads;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int)}. Since it will be re-used to display
     * different items in the data set, it is a good idea to cache references to sub views of
     * the View to avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @NonNull
    @Override
    public SelfieGalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selfie_thumbnail, parent, false);

        return new ViewHolder(itemView);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method should
     * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
     * position.
     * <p>
     * Note that unlike {@link ListView}, RecyclerView will not call this method
     * again if the position of the item changes in the data set unless the item itself is
     * invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside
     * this method and should not keep a copy of it. If you need the position of an item later
     * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
     * have the updated adapter position.
     * <p>
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull SelfieGalleryAdapter.ViewHolder holder, int position) {
        Upload uploadCurrent = mUploads.get(position);

        Glide.with(mContext)
                .load(uploadCurrent.getImageUrl())
                .into(holder.mThumbnailImageView);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

        void onLongItemClick(int position);
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * <p>
     * <p>{@link RecyclerView.Adapter} implementations should subclass ViewHolder and add fields for caching
     * potentially expensive {@link View#findViewById(int)} results.</p>
     * <p>
     * <p>While {@link RecyclerView.LayoutParams} belong to the {@link RecyclerView.LayoutManager},
     * {@link RecyclerView.ViewHolder ViewHolders} belong to the adapter. Adapters should feel free to use
     * their own custom ViewHolder implementations to store data that makes binding view contents
     * easier. Implementations should assume that individual item views will hold strong references
     * to <code>ViewHolder</code> objects and that <code>RecyclerView</code> instances may hold
     * strong references to extra off-screen item views for caching purposes</p>
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        ImageView mThumbnailImageView;

        ViewHolder(View itemView) {
            super(itemView);
            mThumbnailImageView = itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnItemClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mOnItemClickListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                        ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuItem deleteImage = contextMenu.add(Menu.NONE, 1, 1, "Delete");
            deleteImage.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mOnItemClickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    switch (menuItem.getItemId()) {
                        case 1:
                            mOnItemClickListener.onLongItemClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }
}
