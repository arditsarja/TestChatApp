package com.demo.testchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.demo.testchatapp.Holder.QBUsersHolder;
import com.demo.testchatapp.R;
import com.github.library.bubbleview.BubbleTextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;

import java.util.ArrayList;

/**
 * Created by User on 01/23/2018.
 */

public class ChatMessageAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatMessage> qbChatMessages;
    private LayoutInflater inflater;

    public ChatMessageAdapter(Context context, ArrayList<QBChatMessage> qbChatMessages) {
        this.context = context;
        this.qbChatMessages = qbChatMessages;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return qbChatMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return qbChatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            if (qbChatMessages.get(position).getSenderId().equals(QBChatService.getInstance().getUser().getId()))
                view = inflater.inflate(R.layout.list_send_message, null);
            else {
                view = inflater.inflate(R.layout.list_recive_message, null);
                TextView txtName = view.findViewById(R.id.message_user);
                txtName.setText(QBUsersHolder.getInstance().getUserbyId(qbChatMessages.get(position).getSenderId()).getFullName());
            }
            BubbleTextView message = view.findViewById(R.id.message_content);
            message.setText(qbChatMessages.get(position).getBody());
        }

        return view;
    }
}
