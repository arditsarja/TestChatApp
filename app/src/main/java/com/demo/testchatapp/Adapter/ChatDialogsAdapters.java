package com.demo.testchatapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.demo.testchatapp.Holder.QBUnreadMessageHolder;
import com.demo.testchatapp.R;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by User on 01/18/2018.
 */

public class ChatDialogsAdapters extends BaseAdapter {

    private Context context;
    private ArrayList<QBChatDialog> qbChatDialogs;
    private LayoutInflater inflater;

    public ChatDialogsAdapters(Context context, ArrayList<QBChatDialog> qbChatDialogs) {
        this.context = context;
        this.qbChatDialogs = qbChatDialogs;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return qbChatDialogs.size();
    }

    @Override
    public QBChatDialog getItem(int position) {
        return qbChatDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_chat_dialogs, null);

            TextView txtTitle, txtMessage;
            final ImageView imageView, unreadMessage;
            txtMessage = view.findViewById(R.id.list_chat_dialog_message);
            txtTitle = view.findViewById(R.id.list_chat_dialog_title);
            imageView = view.findViewById(R.id.image_chatDialog);
            unreadMessage = view.findViewById(R.id.image_unread);

            ColorGenerator generator = ColorGenerator.MATERIAL;
            int randomColor = generator.getRandomColor();

            txtMessage.setText(getItem(position).getLastMessage());
            txtTitle.setText(getItem(position).getName());

            if (qbChatDialogs.get(position).getPhoto().equals("null")) {


                TextDrawable.IBuilder builder = TextDrawable.builder().beginConfig()
                        .withBorder(4)
                        .endConfig()
                        .round();


                // GetFirst character fro, chat dialog title for create chat dialog image
                TextDrawable drawable = builder.build(txtTitle.getText().toString().substring(0, 1).toUpperCase(), randomColor);
                imageView.setImageDrawable(drawable);
            } else {
                QBContent.getFile(Integer.parseInt(qbChatDialogs.get(position).getPhoto())).performAsync(new QBEntityCallback<QBFile>() {
                    @Override
                    public void onSuccess(QBFile qbFile, Bundle bundle) {
                        Picasso.with(context).load(qbFile.getPublicUrl())
                                .resize(50, 50)
                                .centerCrop().into(imageView);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("Error picture", e.getMessage());
                    }
                });
            }


            TextDrawable.IBuilder unreadBuilder = TextDrawable.builder().beginConfig()
                    .withBorder(4)
                    .endConfig()
                    .round();
            int unread_count = QBUnreadMessageHolder.getInstance().getBundle().getInt(qbChatDialogs.get(position).getDialogId());
            if (unread_count > 0) {
                TextDrawable unread_drawable = unreadBuilder.build("" + unread_count, Color.RED);
                unreadMessage.setImageDrawable(unread_drawable);
            }


        }


        return view;
    }
}
