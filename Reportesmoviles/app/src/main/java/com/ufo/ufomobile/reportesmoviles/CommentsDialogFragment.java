package com.ufo.ufomobile.reportesmoviles;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utilities.Category;
import utilities.Comment;
import utilities.DBHelper;
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

    //Logical Elements
    private ArrayList<Comment> commentArrayList=new ArrayList<>();
    private DBHelper db;
    private User user;

    /**
     * Crreates a new instance of the dialog
     * @return
     */
    public  static CommentsDialogFragment newInstance() {

        Bundle args = new Bundle();

        CommentsDialogFragment fragment = new CommentsDialogFragment();
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

        commentsRecycler = (RecyclerView)view.findViewById(R.id.comments_recyclerview);
        editComment = (EditText) view.findViewById(R.id.comment_edit);
        btnSend = (ImageView) view.findViewById(R.id.btn_send);

        // TODO: 5/12/16 cargar comentarios
        commentArrayList.add(new Comment("Santiago Moreno Benavides","Comentario de prueba. Quemado en codigo, es necesaria la conexión a la Base de datos"));
        commentArrayList.add(new Comment("Santiago Moreno Benavides","Comentario de prueba 2. Quemado en codigo, es necesaria la conexión a la Base de datos"));
        commentArrayList.add(new Comment("Santiago Moreno Benavides","Comentario de prueba 3. Quemado en codigo, es necesaria la conexión a la Base de datos"));

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        adapter = new Recycler_View_Adapter(commentArrayList, getActivity().getApplicationContext());
        commentsRecycler.setAdapter(adapter);
        commentsRecycler.setLayoutManager(layoutManager);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editComment.getText().toString();
                if(TextUtils.isEmpty(comment)){
                    editComment.setError(getString(R.string.field_required_error));
                    editComment.requestFocus();
                }else{
                    // TODO: 5/12/16 Enviar comentario
                    Comment comment1 = new Comment(user.getName(),comment);
                    adapter.insert(adapter.getItemCount(),comment1);
                    editComment.setText("");
                    layoutManager.scrollToPosition(adapter.getItemCount());
                }
            }
        });

        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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


}
