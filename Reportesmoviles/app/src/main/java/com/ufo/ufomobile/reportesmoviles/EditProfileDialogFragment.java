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

import utilities.Category;

/**
 * Created by Tiago on 15/08/16.
 */
public class EditProfileDialogFragment extends DialogFragment {
    private int PICK_IMAGE_REQUEST = 1;

    private String actName,actIdNumber,actPhone,actPicture,newPicture,oldPassword;
    private EditText name,phone,oldPassTxt,newPassTxt,renewPassTxt;
    private ImageView picture;
    private Button done;
    OnaEditProfileSelected mListener;
    private Bitmap bitmap;
    private Uri filePath;
    private LruCache<String, Bitmap> mMemoryCache;
    private boolean isExpanded;

    public interface OnaEditProfileSelected {
        public void onEditProfileSelectedListener(String name,String idNum,String phone,String picture,String newPassword);
    }

    public EditProfileDialogFragment(){

    }

    public static EditProfileDialogFragment newInstance(String name,String idNum,String phone, String picture,String password){
        EditProfileDialogFragment f = new EditProfileDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("name",name);
        args.putString("idNum",idNum);
        args.putString("phone",phone);
        args.putString("picture",picture);
        args.putString("password",password);
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
        oldPassword=getArguments().getString("password");
        //------------------------------------------------------------------------------------------
        name=(EditText)view.findViewById(R.id.name);
        phone=(EditText)view.findViewById(R.id.phone);
        picture=(ImageView)view.findViewById(R.id.pic);
        oldPassTxt=(EditText)view.findViewById(R.id.edit_password);
        newPassTxt=(EditText)view.findViewById(R.id.edit_new_password);
        renewPassTxt=(EditText)view.findViewById(R.id.edit_re_new_password);
        View restoreLayoutBtn = (View) view.findViewById(R.id.restore_layout);
        final View passwordLayout = (View) view.findViewById(R.id.password_layout);
        final ImageView expandBtn = (ImageView) view.findViewById(R.id.expand_btn);

        name.setText(actName);
        phone.setText(actPhone);

        done=(Button)view.findViewById(R.id.done);

        //Click on donde
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nam=name.getText().toString();
                String pho=phone.getText().toString();
                boolean cancel = false;
                if(isExpanded) {
                    //Update the password
                    // TODO: 9/12/16
                    String oldPass = oldPassTxt.getText().toString();
                    String newPass = newPassTxt.getText().toString();
                    String renewPass = renewPassTxt.getText().toString();

                    if(!oldPass.equals("") && !newPass.equals("") && !renewPass.equals("")){
                        boolean oldpassmatch = passMatch(oldPassword,oldPass);
                        boolean newpassmatch = passMatch(newPass,renewPass);

                        if(!oldpassmatch){
                            cancel = true;
                            oldPassTxt.setError(getString(R.string.no_match_password_error));
                            oldPassTxt.requestFocus();
                        }else if(!newpassmatch){
                            cancel = true;
                            newPassTxt.setError(getString(R.string.no_match_password_error));
                            newPassTxt.requestFocus();
                        }
                    }else{
                        cancel = true;
                    }

                    if (!nam.equals("") && !pho.equals("") && !cancel) {
                        mListener.onEditProfileSelectedListener(nam, actIdNumber, pho, newPicture, newPass);
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.incomplete_info_error), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    //Don't update the passwors
                    if (!nam.equals("") && !pho.equals("")) {
                        mListener.onEditProfileSelectedListener(nam, actIdNumber, pho, newPicture, null);
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(),
                                getResources().getString(R.string.incomplete_info_error), Toast.LENGTH_SHORT).show();
                    }
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

        passwordLayout.setVisibility(View.GONE);

        //Click to expand
        restoreLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExpanded){
                    passwordLayout.setVisibility(View.GONE);
                    isExpanded = false;
                    expandBtn.setImageResource(R.drawable.ic_expand_more_white_48dp);
                }else {
                    passwordLayout.setVisibility(View.VISIBLE);
                    isExpanded = true;
                    expandBtn.setImageResource(R.drawable.ic_expand_less_blue_48dp);
                }
            }
        });

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
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

    /**
     * Verify the amtches between passwords
     * @param pass1
     * @param pass2
     * @return
     */
    private boolean passMatch(String pass1,String pass2){
        if(pass1.equals(pass2)){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Show the file chooser
     */
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

    /**
     * Get the base64 image string
     * @param bmp
     * @return
     */
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
}
