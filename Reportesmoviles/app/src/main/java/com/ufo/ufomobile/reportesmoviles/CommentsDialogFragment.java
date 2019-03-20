package com.ufo.ufomobile.reportesmoviles;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import utilities.Category;
import utilities.Comment;
import utilities.Constants;
import utilities.DBHelper;
import utilities.JSONParser;
import utilities.Report;
import utilities.User;

/**
 * Created by Tiago on 5/12/16.
 */
public class CommentsDialogFragment extends DialogFragment {

    //UI Elements
    private RecyclerView commentsRecycler;
    private Recycler_View_Adapter adapter;
    private EditText editComment;
    private ImageView btnSend;
    private ProgressBar loader;
    LinearLayoutManager layoutManager;
    ProgressDialog progressBar;

    //Logical Elements
    private ArrayList<Comment> commentArrayList=new ArrayList<>();
    private DBHelper db;
    private User user;
    private String reportId;

    /**
     * Crreates a new instance of the dialog
     * @return
     */
    public  static CommentsDialogFragment newInstance(String reportId) {

        Bundle args = new Bundle();

        CommentsDialogFragment fragment = new CommentsDialogFragment();
        args.putString(Constants.REPORT_EXTRA,reportId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Empty constructor
     */
    public CommentsDialogFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view=inflater.inflate(R.layout.comments_dialog, container, false);
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db=new DBHelper(getActivity().getApplicationContext());
        user = db.userExists();
        reportId = getArguments().getString(Constants.REPORT_EXTRA);

        commentsRecycler = (RecyclerView)view.findViewById(R.id.comments_recyclerview);
        editComment = (EditText) view.findViewById(R.id.comment_edit);
        btnSend = (ImageView) view.findViewById(R.id.btn_send);
        loader = (ProgressBar) view.findViewById(R.id.loader);

        // Load the comments
        new GetCommentAsync(createJsonReportId(reportId),Constants.URL_GET_COMMENTS,getActivity().getApplicationContext()).execute();

        //Click on send comment
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editComment.getText().toString();
                if(TextUtils.isEmpty(comment)){
                    //Nothing to comment
                    editComment.setError(getString(R.string.field_required_error));
                    editComment.requestFocus();
                }else{
                    // TODO: 5/12/16 Show progress dialog when send a coment
                    //Create the json object to send
                    JSONObject jsonObject = createJsonComent(user.getId(),reportId,comment);
                    new CreateCommentAsync(jsonObject,Constants.URL_COMMENT,getActivity().getApplicationContext(),comment).execute();
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
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    /**
     * Prepare the info in a json object
     * @param idUser
     * @param idReport
     * @param comment
     * @return
     */
    private JSONObject createJsonComent(String idUser,String idReport,String comment){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.USER_EXTRA,idUser);
        map.put(Constants.REPORT_EXTRA,idReport);
        map.put(Constants.COMMENT_EXTRA,comment);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject;
    }

    /**
     * Prepare the info in a json object
     * @param reportId
     * @return
     */
    private JSONObject createJsonReportId(String reportId){
        HashMap<String,String> map = new HashMap<>();
        map.put(Constants.REPORT_EXTRA,reportId);

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject;
    }

    //--------------------------------------------------------------------------------------------------------------
    //RECYCLER VIEW ADAPTER FOR HORIZONTAL SCROLL
    //--------------------------------------------------------------------------------------------------------------
    public class Recycler_View_Adapter extends RecyclerView.Adapter<View_Holder> {

        List<Comment> list = Collections.emptyList();
        Context context;

        public Recycler_View_Adapter(List<Comment> list, Context context) {
            this.list = list;
            this.context = context;
        }

        @Override
        public View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Inflate the layout, initialize the View Holder
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
            View_Holder holder = new View_Holder(v);

            return holder;

        }

        @Override
        public void onBindViewHolder(View_Holder holder, final int position) {

            String name=list.get(position).getIdUsuario();
            String comment=list.get(position).getComentario();
            //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
            holder.comment.setText(comment);
            holder.name.setText(name);

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
        public void insert(int position, Comment data) {
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

        TextView name,comment;//,more;

        View_Holder(View itemView) {
            super(itemView);
            this.setIsRecyclable(false);
            name = (TextView) itemView.findViewById(R.id.name);
            comment = (TextView) itemView.findViewById(R.id.comment);
        }
    }

    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class CreateCommentAsync extends AsyncTask<Void, Void, JSONObject> {
        /**
         * Params
         */
        private JSONObject data;
        private String url;
        private String comment;
        private Context context;
        private JSONParser connectionHelper;

        /**
         *
         * @param data
         * @param url
         * @param context
         */
        public CreateCommentAsync(JSONObject data,String url,Context context,String comment){
            this.data = data;
            this.url = url;
            this.context = context;
            this.comment = comment;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /**
            progressBar = new ProgressDialog(getActivity().getApplicationContext());
            progressBar.setCancelable(true);
            progressBar.setMessage(context.getString(R.string.sending_data));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.show();
             */
            loader.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject response;
            response = connectionHelper.sendPOST(data,url);
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if(jsonObject!=null) {
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(getActivity().getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();

                        Comment comment1 = new Comment(user.getName(),comment);
                        adapter.insert(adapter.getItemCount(),comment1);
                        editComment.setText("");
                        layoutManager.scrollToPositionWithOffset(adapter.getItemCount(),20);

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(getActivity().getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            //progressBar.dismiss();
            loader.setVisibility(View.GONE);
        }
    }

    /**---------------------------------------------------------------------------------------------
     * ASYNC TASK: Send data to the server
     *----------------------------------------------------------------------------------------------
     */
    public class GetCommentAsync extends AsyncTask<Void, Void, JSONObject> {
        /**
         * Params
         */
        private JSONObject data;
        private String url;
        private Context context;
        private JSONParser connectionHelper;

        /**
         *
         * @param data
         * @param url
         * @param context
         */
        public GetCommentAsync(JSONObject data,String url,Context context){
            this.data = data;
            this.url = url;
            this.context = context;
            connectionHelper = new JSONParser(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            JSONObject response;
            response = connectionHelper.sendPOST(data,url);
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if(jsonObject!=null) {
                try {
                    int successCode = jsonObject.getInt(Constants.STATUS_EXTRA);
                    if(successCode==Constants.SUCCESS_CODE) {
                        //Show the message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(getActivity().getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();
                        // Fill the list with the comments
                        JSONArray commentsArr = jsonObject.getJSONArray(Constants.COMMENTS_EXTRA);
                        for (int i = 0; i < commentsArr.length(); i++){
                            JSONObject commentObj = (JSONObject) commentsArr.get(i);
                            String name = commentObj.getString(Constants.NAME_EXTRA);
                            String comment = commentObj.getString(Constants.COMMENT_EXTRA);

                            Comment c = new Comment(name,comment);
                            commentArrayList.add(c);
                        }


                        layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                        adapter = new Recycler_View_Adapter(commentArrayList, getActivity().getApplicationContext());
                        commentsRecycler.setAdapter(adapter);
                        commentsRecycler.setLayoutManager(layoutManager);

                    }else {
                        // Show the error message
                        String errorMessage = jsonObject.getString(Constants.MESSAGE_EXTRA);
                        Toast.makeText(getActivity().getApplicationContext(),errorMessage,Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                // Show the error message
                Toast.makeText(getActivity().getApplicationContext(),getString(R.string.bad_connection_error),Toast.LENGTH_LONG).show();
            }
            //progressBar.dismiss();
            loader.setVisibility(View.GONE);
        }
    }


}
