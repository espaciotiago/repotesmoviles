package com.ufo.ufomobile.reportesmoviles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utilities.Report;

public class ReportDescriptionActivity extends AppCompatActivity {


    TextView date,id,title,description,address,referencePoint,status,supports;
    Recycler_View_Adapter adapter;
    List<Bitmap> data;
    ImageButton mapview;
    ImageView categoryImage;
    int resource_category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_description);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Report report = (Report) getIntent().getSerializableExtra("report");
        String stat = report.getStatus();
        String cat = report.getCategory();
        String[] categories=getResources().getStringArray(R.array.categories);
        int[] imageIDs = {
                R.drawable.alcantarillado,
                R.drawable.alumbrado,
                R.drawable.acueducto,
                R.drawable.basura,
                R.drawable.limpieza,
                R.drawable.gas,
        };

        //------------------------------------------------------------------------------------------
        date = (TextView)findViewById(R.id.date);
        id = (TextView)findViewById(R.id.id);
        title = (TextView)findViewById(R.id.title);
        description = (TextView)findViewById(R.id.description);
        address = (TextView)findViewById(R.id.address);
        referencePoint = (TextView)findViewById(R.id.reference_point);
        status = (TextView)findViewById(R.id.status);
        supports= (TextView) findViewById(R.id.supports);
        categoryImage = (ImageView)findViewById(R.id.category__img);

        date.setText(report.getDate());
        id.setText(report.getId());
        title.setText(report.getTitle());
        description.setText(report.getDescription());
        address.setText(report.getAddress());
        referencePoint.setText(report.getReferencePoint());
        status.setText(report.getStatus());
        supports.setText(report.getSupports()+"");

        if(cat.equals(categories[0])){
            categoryImage.setImageResource(imageIDs[0]);
            resource_category=imageIDs[0];
        }else if(cat.equals(categories[1])){
            categoryImage.setImageResource(imageIDs[1]);
            resource_category=imageIDs[1];
        }else if(cat.equals(categories[2])){
            categoryImage.setImageResource(imageIDs[2]);
            resource_category=imageIDs[2];
        }else if(cat.equals(categories[3])){
            categoryImage.setImageResource(imageIDs[3]);
            resource_category=imageIDs[3];
        }else if(cat.equals(categories[4])){
            categoryImage.setImageResource(imageIDs[4]);
            resource_category=imageIDs[4];
        }else if(cat.equals(categories[5])){
            categoryImage.setImageResource(imageIDs[5]);
            resource_category=imageIDs[5];
        }

        if(stat.equals(Report.IN_PROCESS)){
            status.setBackgroundColor(getResources().getColor(R.color.colorInPorcess));
        }else if(stat.equals(Report.PUBLISHED)){
            status.setBackgroundColor(getResources().getColor(R.color.colorPublished));
        }else if(stat.equals(Report.SOLVED)){
            status.setBackgroundColor(getResources().getColor(R.color.colorSolved));
        }

        //Horizontal list -------------------------------------------------------------------------
        data = new ArrayList<Bitmap>();
        data.add(null);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView myList = (RecyclerView) findViewById(R.id.img_recycler_view);
        adapter = new Recycler_View_Adapter(data, getApplication());
        myList.setAdapter(adapter);
        myList.setLayoutManager(layoutManager);

        //Button -----------------------------------------------------------------------------------
        mapview=(ImageButton)findViewById(R.id.address_map_view);
        mapview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = report.getLatitude();
                double lon = report.getLongitude();
                String addr = report.getAddress();
                AddressViewDialogFragment newFragment;
                FragmentManager fragmentManager = getSupportFragmentManager();
                newFragment = AddressViewDialogFragment.newInstance(lat, lon,addr,resource_category);
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.add(android.R.id.content, newFragment).addToBackStack(null).commit();
            }
        });

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
            }
            else{

            }
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
            list.add(position, data);
            notifyItemInserted(position);
        }

        // Remove a RecyclerView item containing a specified Data object
        public void remove(String data) {
            int position = list.indexOf(data);
            list.remove(position);
            notifyItemRemoved(position);
        }

    }

    //Auxiliar class holder ------------------------------------------------------------
    public class View_Holder extends RecyclerView.ViewHolder {

        ImageView image;

        View_Holder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }

}
