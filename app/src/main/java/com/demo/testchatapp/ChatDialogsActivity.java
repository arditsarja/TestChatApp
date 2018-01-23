package com.demo.testchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.demo.testchatapp.Adapter.ChatDialogsAdapters;
import com.demo.testchatapp.Common.Common;
import com.demo.testchatapp.Holder.QBChatDialogHolder;
import com.demo.testchatapp.Holder.QBChatMessageHolder;
import com.demo.testchatapp.Holder.QBUsersHolder;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.BaseService;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class ChatDialogsActivity extends AppCompatActivity implements QBSystemMessageListener{

    FloatingActionButton floatingActionButton;
    ListView lstChatDialogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_dialogs);
        createSessionForChat();
        lstChatDialogs = findViewById(R.id.lstChatDialog);
        lstChatDialogs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog qbChatDialog = (QBChatDialog) lstChatDialogs.getAdapter().getItem(position);

                Intent intent = new Intent(ChatDialogsActivity.this,ChatMessageActivity.class);
                intent.putExtra(Common.DIALOG_EXTRA,qbChatDialog);
                startActivity(intent);
            }
        });
        loadChatDialogs();
        floatingActionButton = (FloatingActionButton) findViewById(R.id.chatdialog_adduser);

    }

    @Override
    protected void onResume(){
        super.onResume();
        loadChatDialogs();
    }

    public void addChatDoalogUser(View view){
        Intent intent = new Intent(ChatDialogsActivity.this,ListUsersActivity.class);
        startActivity(intent);
    }

    private void loadChatDialogs() {
        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        requestGetBuilder.setLimit(100);
        QBRestChatService.getChatDialogs(null,requestGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                //put all dialogs in cache
                QBChatDialogHolder.getInstance().putChatDialogs(qbChatDialogs);
                ChatDialogsAdapters adapters = new ChatDialogsAdapters(getBaseContext(),qbChatDialogs);
                lstChatDialogs.setAdapter(adapters);
                adapters.notifyDataSetChanged();

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }

    private void createSessionForChat() {

        final ProgressDialog mDialog = new ProgressDialog(ChatDialogsActivity.this);
        mDialog.setMessage("Please waiting");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();


        final String user, password;
        user = getIntent().getStringExtra("user");
        password = getIntent().getStringExtra("password");

        //Load all user and save to cache
        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                QBUsersHolder.getInstance().putUsers(qbUsers);
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });



        final QBUser qbUser = new QBUser(user, password);
        QBAuth.createSession(qbUser).performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                qbUser.setId(qbSession.getUserId());
//                qbUser.setPassword(password);

                try {

                    qbUser.setPassword(BaseService.getBaseService().getToken());
                } catch (BaseServiceException e) {
                    e.printStackTrace();
                }

                QBChatService.getInstance().login(qbUser, new QBEntityCallback() {
                    @Override
                    public void onSuccess(Object o, Bundle bundle) {
                        mDialog.dismiss();

                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
                        qbSystemMessagesManager.addSystemMessageListener(ChatDialogsActivity.this);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        e.printStackTrace();
                        Log.e("Error", e.getMessage());
                        mDialog.dismiss();
                    }
                });

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });

    }



    @Override
    public void processMessage(QBChatMessage qbChatMessage) {

       QBRestChatService.getChatDialogById(qbChatMessage.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
           @Override
           public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
               //put to cache
               QBChatDialogHolder.getInstance().putChatDialog(qbChatDialog);
               ArrayList<QBChatDialog> qbChatDialogs =  QBChatDialogHolder.getInstance().getAllDialogs();
               ChatDialogsAdapters adapters = new ChatDialogsAdapters(getBaseContext(),qbChatDialogs);
               lstChatDialogs.setAdapter(adapters);
               adapters.notifyDataSetChanged();

           }

           @Override
           public void onError(QBResponseException e) {

           }
       });
    }

    @Override
    public void processError(QBChatException e, QBChatMessage qbChatMessage) {
        Log.e("ERROR",e.getMessage());
    }
}
