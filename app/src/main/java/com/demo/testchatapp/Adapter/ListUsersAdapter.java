package com.demo.testchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by User on 01/19/2018.
 */

public class ListUsersAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBUser> qbUsers;
    private LayoutInflater inflater;

    public ListUsersAdapter(Context context, ArrayList<QBUser> qbUsers) {

        this.context = context;
        this.qbUsers = qbUsers;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return qbUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return qbUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            view =  inflater.inflate(android.R.layout.simple_list_item_multiple_choice,null);
            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(qbUsers.get(position).getLogin());


        }

        return view;
    }
}
