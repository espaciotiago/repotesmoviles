package com.ufo.ufomobile.reportesmoviles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import utilities.Constants;
import utilities.Report;

public class AddNewReportActivity extends AppCompatActivity implements AddressPickerDialogFragment.OnaAddSelected {

    private int categoryResource;
    private double latitude,longitude;

    private ImageView categoryImg;
    private ImageButton addImage;
    private EditText title,description,address;
    private Button send;
    private ScrollView scrollView;
    private ImageButton mapview;
    private String categoryName;
    RecyclerView myList;
    Recycler_View_Adapter adapter;
    List<Bitmap> data;
    ArrayList<String> images = new ArrayList<String>();
    ArrayList<String> imagesName = new ArrayList<String>();

    int total_imgs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoryName=getIntent().getStringExtra("category_name");

        scrollView=(ScrollView)findViewById(R.id.scrollView);
        title=(EditText)findViewById(R.id.title);
        description=(EditText)findViewById(R.id.description);
        address=(EditText)findViewById(R.id.address);
        addImage = (ImageButton) findViewById(R.id.add_image);

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                AddressPickerDialogFragment dialog = AddressPickerDialogFragment.newInstance(categoryResource);
                dialog.show(fm, "dialog");
            }
        });

        mapview = (ImageButton)findViewById(R.id.mapview);
        mapview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                AddressPickerDialogFragment dialog = AddressPickerDialogFragment.newInstance(categoryResource);
                dialog.show(fm, "dialog");
            }
        });

        categoryResource = getIntent().getIntExtra("category_icon",0);
        categoryImg=(ImageView)findViewById(R.id.category__img);
        categoryImg.setImageResource(categoryResource);

        description=(EditText)findViewById(R.id.description);
        description.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                scrollView.requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(total_imgs<4) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 0);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Número máximo de imagenes alcanzado",Toast.LENGTH_SHORT).show();
                }
            }
        });


        //Horizontal list -------------------------------------------------------------------------
        //----------------------------------------------------------------------------------------
        data = new ArrayList<Bitmap>();
        //----------------------------------------------------------------------------------------
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        myList = (RecyclerView) findViewById(R.id.img_recycler_view);
        adapter = new Recycler_View_Adapter(data, getApplication());
        myList.setAdapter(adapter);
        myList.setLayoutManager(layoutManager);

        //Send button ------------------------------------------------------------------------------
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tit=title.getText().toString();
                String desc=description.getText().toString();
                String addr=address.getText().toString();
                DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                String date = df.format(Calendar.getInstance().getTime());


                //Populate the array of string images
                for(int i =0;i<data.size();i++){
                    Log.d("NAME IMAGE",imagesName.get(i));
                    Log.d("STRING IMAGE",images.get(i));
                }


                if(!tit.equals("") && !desc.equals("") && !addr.equals("")){
                    //Creates the new report
                    Report report = new Report("00",tit,desc,addr,
                            latitude,0,longitude,Report.PUBLISHED,categoryName,date,images);
                    //TODO Enviar reporte
                    Toast.makeText(getApplicationContext(),
                            data.size()+"",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.incomplete_info_error),Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Open the dialog at first
        FragmentManager fm = getSupportFragmentManager();
        AddressPickerDialogFragment dialog = AddressPickerDialogFragment.newInstance(categoryResource);
        dialog.show(fm, "dialog");
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param imageReturnedIntent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        Bitmap bp = decodeUri(getApplicationContext(), selectedImage);
                        int w=256;
                        int h=256;
                        Bitmap thumbImage = ThumbnailUtils.extractThumbnail(bp,w,h);
                        adapter.insert(adapter.getItemCount(), thumbImage);
                        //myList.scrollToPosition(adapter.getItemCount());
                        // TODO: 15/11/16 agregar imagen a arreglo de imagenes
                        //data.add(bp);
                        total_imgs++;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                break;
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = imageReturnedIntent.getData();
                    try {
                        Bitmap bp = decodeUri(getApplicationContext(), selectedImage);
                        //adapter.insert(0, Bitmap.createScaledBitmap(bp, w, h, false));
                        //adapter.insert(0,null);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public static Bitmap decodeUri(Context c, Uri uri)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 30, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    @Override
    public void onArticleSelectedListener(String addr,double latitude,double longitude) {
        address.setText(addr);
        this.latitude=latitude;
        this.longitude=longitude;
        Log.d("TAG LONG","long: "+this.longitude+ " lat: " +this.latitude);
    }


    //--------------------------------------------------------------------------------------------------------------
    //RECYCLER VIEW ADAPTER FOR HORIZONTAL SCROLL
    //--------------------------------------------------------------------------------------------------------------
    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {

        List<Bitmap> list = Collections.emptyList();
        Context context;

        public Recycler_View_Adapter(List<Bitmap> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row_layout, parent, false);
            View_Holder holder = new View_Holder(v);

            return holder;

        }

        @Override
        public void onBindViewHolder(View_Holder holder, final int position) {

             //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            if(list.get(position)!=null){
                holder.image.setImageBitmap(list.get(position));
                holder.delete.setVisibility(View.VISIBLE);
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.remove(position);
                    }
                });
            }
            /*
            else{
                holder.delete.setVisibility(View.GONE);
                holder.image.setBackgroundResource(R.drawable.add_image_background);
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //data.add(0,null);
                        //adapter.insert(0,null);
                        if(total_imgs<4) {
                            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);
                        }else{
                            Toast.makeText(getApplicationContext(),
                                    "Número máximo de imagenes alcanzado",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            */
            //holder.image.setText(list.get(position).getTitle());

            //animate(holder);

        }

        @Override
        public int getItemCount() {
            //returns the number of elements the RecyclerView will display
            return list.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        // Insert a new item to the RecyclerView on a predefined position
        public void insert(int position, Bitmap data) {
            // TODO: 15/11/16 agregar imagen a arreglo de imagenes
            list.add(position, data);
            images.add(Constants.getStringImage(data));
            imagesName.add(Constants.giverImageName(Constants.IMG_CONS_REP));
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing a specified Data object
        public void remove(int position) {
            //int position = list.indexOf(data);
            // TODO: 15/11/16 elimiar imagen de arreglo de imagenes
            Log.d("POS DEL",position+"");
            total_imgs--;
            list.remove(position);
            images.remove(position);
            imagesName.remove(position);
            notifyItemRemoved(position);

        }

    }

    //Auxiliar class holder ------------------------------------------------------------------------
    public class View_Holder extends RecyclerView.ViewHolder {

        ImageView image,delete;

        View_Holder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            delete = (ImageView) itemView.findViewById(R.id.delete_btn);
        }
    }

    //----------------------------------------------------------------------------------------------

}
