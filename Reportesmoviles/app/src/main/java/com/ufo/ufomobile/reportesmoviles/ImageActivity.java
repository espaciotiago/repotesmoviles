package com.ufo.ufomobile.reportesmoviles;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import me.relex.circleindicator.CircleIndicator;
import utilities.Constants;
import utilities.JSONParser;

public class ImageActivity extends AppCompatActivity {

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 4;

    private ArrayList<String> images;
    private String idReport;
    private int posSelected;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    ProgressBar loader2;
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        idReport = getIntent().getStringExtra("reportId");
        posSelected = getIntent().getIntExtra("pos",0);

        loader2 = (ProgressBar) findViewById(R.id.loader2);
        images = new ArrayList();//getIntent().getStringArrayListExtra("images");
        JSONObject jsonObject = createJsonGetImages(idReport);
        new Http_GetReportImages(jsonObject,Constants.URL_GET_REPORT_IMAGES,ImageActivity.this).execute();
    }

    /**
     * Prepare the info to send and get the encoded images
     * @param idReport
     * @return
     */
    private JSONObject createJsonGetImages(String idReport){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.ID_EXTRA,idReport);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject;
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ImageSlidePagerFragment.newInstance(position,images);
        }

        @Override
        public int getCount() {
            return images.size();
        }
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

    //----------------------------------------------------------------------------------------------
    //ASYNC TASK GETTING THE IMAGES OF THE REPORT
    //----------------------------------------------------------------------------------------------
    private class Http_GetReportImages extends AsyncTask<Void, Void, JSONObject>
    {
        private JSONObject jsonObject;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        public Http_GetReportImages(JSONObject data,String url,Context context){
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
            /*
            loading = new ProgressDialog(ReportDescriptionActivity.this,R.style.StyledDialog);
            loading.setInverseBackgroundForced(true);
            loading.setCancelable(false);
            loading.show();
            */
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
                // TODO: 8/12/16
                Log.d("JSON IMGS",info.toString());
                try {
                    JSONArray jsonImagesArray = info.getJSONArray(Constants.IMAGES_REPORT_EXTRA);
                    for(int i = 0; i < jsonImagesArray.length();i++){
                        JSONObject jsonImage = (JSONObject) jsonImagesArray.get(i);
                        String image = jsonImage.getString(Constants.IMAGE_EXTRA);
                        images.add(image);
                    }
                } catch (JSONException e) {
                    // TODO: 8/12/16 Show error
                    e.printStackTrace();
                }
            }else{
                // TODO: 8/12/16 Show Error
            }

            // Instantiate a ViewPager and a PagerAdapter.
            mPager = (ViewPager) findViewById(R.id.pager);
            CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
            mPager.setAdapter(mPagerAdapter);
            indicator.setViewPager(mPager);

            mPager.setCurrentItem(posSelected);
            loader2.setVisibility(View.GONE);

        }
    }

}
