package com.ufo.ufomobile.reportesmoviles;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import utilities.Category;

/**
 * Created by 'Santiago on 31/7/2016.
 */
public class CategorySelectionDialogFragment extends DialogFragment {

    OnaAddSelected mListener;
    ListView categories;
    ArrayList<Category> categoryList;

    public interface OnaAddSelected {
        public void onArticleSelectedListener(int resource, String name);
    }

    public  static CategorySelectionDialogFragment newInstance() {

        Bundle args = new Bundle();

        CategorySelectionDialogFragment fragment = new CategorySelectionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // Inflate the layout to use as dialog or embedded fragment
        View view=inflater.inflate(R.layout.category_selection_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] categoriesStr = getResources().getStringArray(R.array.categories);
        categoryList=new ArrayList<Category>();
        categoryList.add(new Category(categoriesStr[0],R.drawable.ic_marker_water));
        categoryList.add(new Category(categoriesStr[1],R.drawable.ic_marker_trash));
        categoryList.add(new Category(categoriesStr[2],R.drawable.ic_marker_traffic));
        categoryList.add(new Category(categoriesStr[3],R.drawable.ic_marker_public));
        categoryList.add(new Category(categoriesStr[4],R.drawable.ic_marker_road));
        categoryList.add(new Category(categoriesStr[5],R.drawable.ic_marker_animal));
        categoryList.add(new Category(categoriesStr[6],R.drawable.ic_marker_police));
        categoryList.add(new Category(categoriesStr[7],R.drawable.ic_marker_other));

        categories=(ListView)view.findViewById(R.id.categories);
        categories.setAdapter(new ListAdapter(getActivity(), categoryList));

        categories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Category cat = categoryList.get(i);
                String name=cat.getName();
                int resource=cat.getResource();
                mListener.onArticleSelectedListener(resource,name);
                dismiss();
            }
        });
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnaAddSelected) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    //----------------------------------------------------------------------------------------------------------------------------
    public class ListAdapter extends BaseAdapter {

        private Context context;
        private List<Category> list;

        public ListAdapter(Context context, List<Category> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            if (convertView == null) {
                // Create a new view into the list.
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(R.layout.category_selection_item, parent, false);
            }

            // Set data into the view.
            TextView name = (TextView) rowView.findViewById(R.id.name);
            ImageView resource = (ImageView) rowView.findViewById(R.id.resource);

            Category item = this.list.get(position);
            name.setText(item.getName());
            resource.setImageResource(item.getResource());
            return rowView;
        }
    }
}
