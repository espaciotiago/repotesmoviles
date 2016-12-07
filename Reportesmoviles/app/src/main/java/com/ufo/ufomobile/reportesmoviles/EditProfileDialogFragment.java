package com.ufo.ufomobile.reportesmoviles;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.LruCache;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Tiago on 15/08/16.
 */
public class EditProfileDialogFragment extends DialogFragment {
    private int PICK_IMAGE_REQUEST = 1;

    private String actName,actIdNumber,actPhone,actPicture,newPicture;
    private EditText name,phone;
    private ImageView picture;
    private Button done;
    OnaEditProfileSelected mListener;
    private Bitmap bitmap;
    private Uri filePath;
    private LruCache<String, Bitmap> mMemoryCache;

    public interface OnaEditProfileSelected {
        public void onEditProfileSelectedListener(String name,String idNum,String phone,String picture);
    }

    public EditProfileDialogFragment(){

    }

    public static EditProfileDialogFragment newInstance(String name,String idNum,String phone, String picture){
        EditProfileDialogFragment f = new EditProfileDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name",name);
        args.putString("idNum",idNum);
        args.putString("phone",phone);
        args.putString("picture",picture);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout to use as dialog or embedded fragment
        View view=inflater.inflate(R.layout.edit_profile_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //------------------------------------------------------------------------------------------
        actName=getArguments().getString("name");
        actIdNumber=getArguments().getString("idNum");
        actPhone=getArguments().getString("phone");
        actPicture=getArguments().getString("picture");
        newPicture=actPicture;
        //------------------------------------------------------------------------------------------
        name=(EditText)view.findViewById(R.id.name);
        phone=(EditText)view.findViewById(R.id.phone);
        picture=(ImageView)view.findViewById(R.id.pic);

        name.setText(actName);
        phone.setText(actPhone);

        done=(Button)view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nam=name.getText().toString();
                String pho=phone.getText().toString();
                if(!nam.equals("") && !pho.equals("")) {
                    mListener.onEditProfileSelectedListener(nam, actIdNumber, pho,newPicture);
                    dismiss();
                }else{
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.incomplete_info_error),Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(!actPicture.equals("") && actPicture!=null){
            Bitmap imag = null;
            byte[] decodedString = Base64.decode(actPicture, Base64.DEFAULT);
            imag = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            picture.setImageBitmap(imag);
        }

        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnaEditProfileSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                //Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap,240,240,false);
                int w=256;
                int h=256;
                Bitmap bitmap2 = ThumbnailUtils.extractThumbnail(bitmap,w,h);
                picture.setImageBitmap(bitmap2);
                newPicture=getStringImage(bitmap2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


    // proccess the image --------------------------------------------------------------------------
    /**
     * Agrega un bitmap al cache
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    /**
     * obtiene un bitmap del cache
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * Execute the load of the image
     * @param resId
     * @param imageView
     */
    public void loadBitmap(int resId, ImageView imageView) {
        if (cancelPotentialWork(resId, imageView)) {

            final String imageKey = String.valueOf(resId);
            final Bitmap bitmap = getBitmapFromMemCache(imageKey);

            if (bitmap != null) {
                BitmapDrawable ob = new BitmapDrawable(getActivity().getResources(),bitmap);
                imageView.setBackground(ob);
            }else {
                Bitmap mPlaceHolderBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(getActivity().getResources(), mPlaceHolderBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(resId);
            }
        }
    }

    /**
     * Cancel the concurrency
     * @param data
     * @param imageView
     * @return
     */
    public static boolean cancelPotentialWork(int data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data) {
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
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        //byte[] decodedString = Base64.decode(user.getImage(), Base64.DEFAULT);
        //BitmapFactory.decodeByteArray(null,0,0,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

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
     * PROCESING THE IMAGE
     */
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private int data = 0;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            Bitmap bitmap = decodeSampledBitmapFromResource(getActivity().getResources(), data, 200, 200);
            addBitmapToMemoryCache(String.valueOf(params[0]), bitmap);
            return bitmap;
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
                    BitmapDrawable ob = new BitmapDrawable(getActivity().getResources(),bitmap);
                    imageView.setBackground(ob);
                }
            }
        }
    }

    /**
     * HANDLE CONCURRENCY
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
}
