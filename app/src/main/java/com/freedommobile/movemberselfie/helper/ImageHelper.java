package com.freedommobile.movemberselfie.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class ImageHelper {
    private static final double SCALING = 0.8;

    /**
     *
     * @param inContext
     * @param inImage
     * @return
     */
    public static Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * @param context
     * @param imageUri
     * @return
     */
    public static String getPath(Context context, Uri imageUri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor =
                context.getContentResolver()
                        .query(imageUri, filePathColumn, null, null, null);

        Objects.requireNonNull(cursor).moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath;
    }

    /**
     * @param source
     * @param angle
     * @return
     */
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Gets a {@link Bitmap} from an {@link Uri} and scales is down.
     *
     * @param context  The {@link Context}.
     * @param imageUri The image {@link Uri}.
     * @return A scaled down {@link Bitmap}.
     */
    public static Bitmap getScaledBitmapFromUri(Context context, Uri imageUri) {
        context.getContentResolver().notifyChange(imageUri, null);
        ContentResolver contentResolver = context.getContentResolver();
        Bitmap bitmapFromUri;

        try {
            bitmapFromUri = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
            return scaleDownBitmap(bitmapFromUri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Scaling down the bitmap to avoid OutOfMemory exceptions.
     *
     * @param originalBitmap The {@link Bitmap} to be scaled down.
     * @return A scaled down {@link Bitmap}.
     */
    private static Bitmap scaleDownBitmap(Bitmap originalBitmap) {
        final int scaledWidth = (int) (originalBitmap.getWidth() * SCALING);
        final int scaledHeight = (int) (originalBitmap.getHeight() * SCALING);

        return Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);
    }
}
