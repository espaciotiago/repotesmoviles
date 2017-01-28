package com.ufo.ufomobile.reportesmoviles;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
import utilities.Report;
import utilities.User;

public class NotificationsActivity extends AppCompatActivity {

    /**
     * UI Elements
     */
    RecyclerView myList;
    Recycler_View_Adapter adapter;
    LinearLayoutManager layoutManager;

    /**
     * Logica elements
     */
    ArrayList<Report> data;
    DBHelper db;
    User user;
    int[] imageIDs = {
            R.drawable.ic_marker_water,
            R.drawable.ic_marker_trash,
            R.drawable.ic_marker_traffic,
            R.drawable.ic_marker_public,
            R.drawable.ic_marker_road,
            R.drawable.ic_marker_animal,
            R.drawable.ic_marker_police,
            R.drawable.ic_marker_other
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Database ---------------------------------------------------------------------------------
        db=new DBHelper(this);
        user = db.userExists();
        //UI ---------------------------------------------------------------------------------------
        layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        myList = (RecyclerView) findViewById(R.id.my_recycler_view);

        JSONObject jsonObject = createGetReportsJson(user.getId());
        new Http_GetReports(jsonObject,Constants.URL_GET_MY_REPORTS,NotificationsActivity.this).execute();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Prepares the info for the request of the reports
     * @param idUsuario
     * @return
     */
    private JSONObject createGetReportsJson(String idUsuario){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.USER_EXTRA,idUsuario);

        JSONObject jsonObject = new JSONObject(map);
        Log.d("JSON GET REPORTS",jsonObject.toString());
        return jsonObject;
    }

    //--------------------------------------------------------------------------------------------------------------
    //RECYCLER VIEW ADAPTER FOR HORIZONTAL SCROLL
    //--------------------------------------------------------------------------------------------------------------
    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {

        List<Report> list = Collections.emptyList();
        Context context;

        public Recycler_View_Adapter(List<Report> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_row_layout, parent, false);
            View_Holder holder = new View_Holder(v);

            return holder;

        }

        @Override
        public void onBindViewHolder(View_Holder holder, final int position) {

            String state= Constants.getStatus(list.get(position).getStatus());
            String category=list.get(position).getCategory();
            String[] categories=getResources().getStringArray(R.array.categories);

            //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            holder.title.setText(list.get(position).getTitle());
            holder.description.setText(list.get(position).getDescription());
            holder.state.setText(state);
            if(state.equals(Report.PUBLISHED)){
                holder.state.setTextColor(getResources().getColor(R.color.colorPublished));
            }else if(state.equals(Report.IN_PROCESS)){
                holder.state.setTextColor(getResources().getColor(R.color.colorInPorcess));
            }else if(state.equals(Report.SOLVED)){
                holder.state.setTextColor(getResources().getColor(R.color.colorSolved));
            }

            if(category.equals(categories[0])){
                holder.category_img.setImageResource(imageIDs[0]);
            }else if(category.equals(categories[1])){
                holder.category_img.setImageResource(imageIDs[1]);
            }else if(category.equals(categories[2])){
                holder.category_img.setImageResource(imageIDs[2]);
            }else if(category.equals(categories[3])){
                holder.category_img.setImageResource(imageIDs[3]);
            }else if(category.equals(categories[4])){
                holder.category_img.setImageResource(imageIDs[4]);
            }else if(category.equals(categories[5])){
                holder.category_img.setImageResource(imageIDs[5]);
            }else if(category.equals(categories[6])){
                holder.category_img.setImageResource(imageIDs[6]);
            }else if(category.equals(categories[7])){
                holder.category_img.setImageResource(imageIDs[7]);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Report report=list.get(position);
                    Intent goToDescription = new Intent(getApplicationContext(),ReportDescriptionActivity.class);
                    goToDescription.putExtra("report",report);
                    startActivity(goToDescription);
                }
            });

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
        public void insert(int position, Report data) {
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

    //Auxiliar class holder ------------------------------------------------------------------------
    public class View_Holder extends RecyclerView.ViewHolder {

        TextView title,state,description;//,more;
        ImageView category_img;

        View_Holder(View itemView) {
            super(itemView);
            this.setIsRecyclable(false);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            state = (TextView) itemView.findViewById(R.id.state);
            //more = (TextView) itemView.findViewById(R.id.more);
            category_img = (ImageView) itemView.findViewById(R.id.category_img);
        }
    }

    //----------------------------------------------------------------------------------------------
    //ASYNC TASK BRINGING MY REPORTS
    //----------------------------------------------------------------------------------------------
    private class Http_GetReports extends AsyncTask<Void, Void, JSONObject>
    {
        ProgressDialog loading;
        private JSONObject jsonObject;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        public Http_GetReports(JSONObject data,String url,Context context){
            this.jsonObject = data;
            this.url = url;
            this.context = context;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loading = ProgressDialog.show(MenuActivity.this, "Comprobando datos...",
            //      "Puede tardar unos segundos", true, true);
            loading = new ProgressDialog(NotificationsActivity.this,R.style.StyledDialog);
            loading.setInverseBackgroundForced(true);
            loading.setCancelable(false);
            loading.show();
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {   JSONObject ret=null;

            try
            {
                ret = connectionHelper.sendPOST(jsonObject,url);
            } catch (Exception e)
            {
                Log.e("MainActivity", e.getMessage(), e);
            }
            return ret;
        }

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        protected void onPostExecute(JSONObject info)
        {

            if(info!=null) {
                Log.d("REPORT ARRAY",info.toString());
                String[] categories = getResources().getStringArray(R.array.categories);
                data = new ArrayList<>();
                //Get the json array of reports
                try {
                    JSONArray jsonArray = info.getJSONArray(Constants.ALL_REPORTS_EXTRA);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonRep = (JSONObject) jsonArray.get(i);
                        Log.d("REPORT JSON", jsonRep.toString());

                        Report rep = new Report(jsonRep.getString(Constants.ID_EXTRA),
                                jsonRep.getString(Constants.USER_EXTRA),
                                jsonRep.getString(Constants.TITLE_EXTRA),
                                jsonRep.getString(Constants.DESCRIPTION_EXTRA),
                                jsonRep.getString(Constants.ADDRESS_EXTRA),
                                jsonRep.getDouble(Constants.LATITUDE_EXTRA),
                                jsonRep.getInt(Constants.SUPPORTS_EXTRA),
                                jsonRep.getInt(Constants.COMMENTS_EXTRA),
                                jsonRep.getInt(Constants.SUPPORTING_EXTRA),
                                jsonRep.getDouble(Constants.LONGITUDE_EXTRA),
                                jsonRep.getString(Constants.STATUS_EXTRA),
                                jsonRep.getString(Constants.CATEGORY_EXTRA),
                                jsonRep.getString(Constants.DATE_EXTRA),
                                new ArrayList<String>());

                        data.add(rep);
                        String category = rep.getCategory();
                        double lat = rep.getLatitude();
                        double lon = rep.getLongitude();
                        LatLng pos = new LatLng(lat, lon);
                    }

                    adapter = new Recycler_View_Adapter(data, getApplication());
                    myList.setAdapter(adapter);
                    myList.setLayoutManager(layoutManager);

                }catch (Exception e){
                    // TODO: 7/12/16 Mostrar pantalla con error de JSON
                }
            }else{
                // TODO: 7/12/16 Mostrar pantalla con error de conexión
            }
            loading.dismiss();
        }
    }

}
