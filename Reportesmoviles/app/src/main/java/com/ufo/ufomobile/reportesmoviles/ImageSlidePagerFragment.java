package com.ufo.ufomobile.reportesmoviles;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import utilities.Constants;
import utilities.JSONParser;

/**
 * Created by Tiago on 29/12/16.
 */
public class ImageSlidePagerFragment extends Fragment {

    public ArrayList<String> images;

    public static  ImageSlidePagerFragment newInstance(int pos,ArrayList<String> images) {

        Bundle args = new Bundle();
        args.putInt("pos",pos);
        args.putStringArrayList("images",images);

        ImageSlidePagerFragment fragment = new ImageSlidePagerFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int pos = getArguments().getInt("pos");
        images = getArguments().getStringArrayList("images");

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.about_item, container, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.image);

        switch (pos){
            case 0:
                loadBitmap(images.get(0),imageView,Constants.IMAGE_COMPLETE);
                break;
            case 1:
                loadBitmap(images.get(1),imageView,Constants.IMAGE_COMPLETE);
                break;
            case 2:
                loadBitmap(images.get(2),imageView,Constants.IMAGE_COMPLETE);
                break;
            case 3:
                loadBitmap(images.get(3),imageView,Constants.IMAGE_COMPLETE);
                break;
        }

        return rootView;
    }

    // ---------------------------------------------------------------------------------
    /**
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     *
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(String resId,
                                                         int reqWidth, int reqHeight, int pos) {

        // First decode with inJustDecodeBounds=true to check dimensions
        byte[] decodedString = Base64.decode(resId, Base64.NO_PADDING);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedString,0,decodedString.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap bp = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length, options);
        if(pos == Constants.IMAGE_COMPLETE){
            return bp;
        }else {
            int w = 256;
            int h = 256;
            Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bp, w, h);
            return bitmap2;
        }
    }

    /**
     * Processing Bitmaps Off the UI Thread
     */
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int pos;
        private String data = "";

        public BitmapWorkerTask(ImageView imageView,int pos) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.pos = pos;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            return decodeSampledBitmapFromResource(data,200,200,pos);
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /**
     *   </ Processing Bitmaps Off the UI Thread >
     */

    /**
     * Handle Concurrency
     */
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     *
     * @param resId
     * @param imageView
     */
    public void loadBitmap(String resId, ImageView imageView,int pos) {
        if (cancelPotentialWork(resId, imageView)) {
            Bitmap mPlaceHolderBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView,pos);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(getResources(), mPlaceHolderBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(resId);
        }
    }

    /**
     *
     * @param data
     * @param imageView
     * @return
     */
    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData.equals("") || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    /**
     *
     * @param imageView
     * @return
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * </ Handle Concurrency >
     */
}
