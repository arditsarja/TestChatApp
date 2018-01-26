package com.demo.testchatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.demo.testchatapp.Adapter.ChatMessageAdapter;
import com.demo.testchatapp.Common.Common;
import com.demo.testchatapp.Holder.QBChatMessageHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.util.ArrayList;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton;
    EditText editContent;


    Toolbar chatMessageGroupToolbar;
    ChatMessageAdapter adapter;

    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.chat_message_content_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        contextMenuIndexClicked = info.position;

        switch (item.getItemId()) {
            case R.id.chat_message_update_menu:
                updateMessage();
                break;
            case R.id.chat_message_delete_menu:
                deleteMessage();
                break;
            default:
                break;
        }
        return true;
    }

    private void deleteMessage() {
        final ProgressDialog progressDialog = new ProgressDialog(ChatMessageActivity.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.show();
        QBRestChatService.deleteMessage(QBChatMessageHolder.getInstance()
                .getChatMessageByDialog(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked).getId(), false).performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                retriveMessage();
                progressDialog.dismiss();
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
            }
        });

    }

    private void updateMessage() {
        //setMessage for edit text
        editMessage = QBChatMessageHolder.getInstance()
                .getChatMessageByDialog(qbChatDialog.getDialogId())
                .get(contextMenuIndexClicked);
        editContent.setText(editMessage.getBody());
        isEditMode = true; // set Edit mode

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        qbChatDialog.removeMessageListrener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initView();
        initChatDialogs();
        retriveMessage();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (qbChatDialog.getType() == QBDialogType.GROUP || qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP)
            getMenuInflater().inflate(R.menu.chat_message_group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_group_name_menu:
                editGroupName();
                break;
            case R.id.add_group_user_menu:
                addUserGroup();
                break;
            case R.id.remove_group_user_menu:
                removeUserGroup();
                break;
            default:
                break;
        }
        return true;
    }

    private void removeUserGroup() {
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_REMOVE_MODE);
        startActivity(intent);
    }

    private void addUserGroup() {
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.putExtra(Common.UPDATE_DIALOG_EXTRA, qbChatDialog);
        intent.putExtra(Common.UPDATE_MODE, Common.UPDATE_ADD_MODE);
        startActivity(intent);
    }

    private void editGroupName() {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View view = inflater.inflate(R.layout.dialog_edit_group_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        final EditText newName = view.findViewById(R.id.edt_new_group_name);
        builder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                qbChatDialog.setName(newName.getText().toString());
                QBDialogRequestBuilder qbDialogRequestBuilder = new QBDialogRequestBuilder();
                QBRestChatService.updateGroupChatDialog(qbChatDialog, qbDialogRequestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        chatMessageGroupToolbar.setTitle(qbChatDialog.getName());
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });AlertDialog alertDialog = builder.create();
        alertDialog.show();


    }

    private void retriveMessage() {
        QBMessageGetBuilder builder = new QBMessageGetBuilder();
        builder.setLimit(500);
        if (qbChatDialog != null) {
            QBRestChatService.getDialogMessages(qbChatDialog, builder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
                @Override
                public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                    // Put message to cache
                    QBChatMessageHolder.getInstance().putMessages(qbChatDialog.getDialogId(), qbChatMessages);
                    adapter = new ChatMessageAdapter(getBaseContext(), qbChatMessages);
                    lstChatMessages.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {

                }
            });
        }

    }

    public void submit(View v) {
        if (!isEditMode) {
            QBChatMessage chatMessage = new QBChatMessage();
            chatMessage.setBody(editContent.getText().toString());
            chatMessage.setSenderId(QBChatService.getInstance().getUser().getId());
            chatMessage.setSaveToHistory(true);
            try {
                qbChatDialog.sendMessage(chatMessage);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

            if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
                QBChatMessageHolder.getInstance().putMessage(qbChatDialog.getDialogId(), chatMessage);
                ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialog(chatMessage.getDialogId());
                adapter = new ChatMessageAdapter(getBaseContext(), messages);
                lstChatMessages.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
            editContent.setText("");
            editContent.setFocusable(true);
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(ChatMessageActivity.this);
            progressDialog.setMessage("Please wait..");
            progressDialog.show();
            QBMessageUpdateBuilder messageUpdateBuilder = new QBMessageUpdateBuilder();
            messageUpdateBuilder.updateText(editContent.getText().toString()).markDelivered().markRead();
            QBRestChatService.updateMessage(editMessage.getId(), qbChatDialog.getDialogId(), messageUpdateBuilder).performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    retriveMessage();
                    editContent.setText("");
                    editContent.setFocusable(true);
                    progressDialog.dismiss();

                    isEditMode = false;
                    editMessage = null;
                }

                @Override
                public void onError(QBResponseException e) {
                    progressDialog.dismiss();
                    isEditMode = false;
                    editMessage = null;
                }
            });
        }
    }

    private void initChatDialogs() {
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.DIALOG_EXTRA);
        qbChatDialog.initForChat(QBChatService.getInstance());
        QBIncomingMessagesManager incomingMessage = QBChatService.getInstance().getIncomingMessagesManager();
        incomingMessage.addDialogMessageListener(new QBChatDialogMessageListener() {
            @Override
            public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

            }

            @Override
            public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

            }
        });


        // Add join group to enable group chat

        if (qbChatDialog.getType() == QBDialogType.PUBLIC_GROUP || qbChatDialog.getType() == QBDialogType.GROUP) {
            DiscussionHistory discussionHistory = new DiscussionHistory();
            discussionHistory.setMaxStanzas(0);
            qbChatDialog.join(discussionHistory, new QBEntityCallback() {
                @Override
                public void onSuccess(Object o, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("Error", e.getMessage());
                }
            });
        }
        chatMessageGroupToolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(chatMessageGroupToolbar);
        qbChatDialog.addMessageListener(this);


    }

    private void initView() {
        lstChatMessages = findViewById(R.id.list_of_message);
        submitButton = findViewById(R.id.send_button);
        editContent = findViewById(R.id.edit_content);
        chatMessageGroupToolbar = findViewById(R.id.chat_message_toolbar);
        //add context menu
        registerForContextMenu(lstChatMessages);

    }


    @Override
    public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
        QBChatMessageHolder.getInstance().putMessage(qbChatMessage.getDialogId(), qbChatMessage);
        ArrayList<QBChatMessage> messages = QBChatMessageHolder.getInstance().getChatMessageByDialog(qbChatMessage.getDialogId());
        adapter = new ChatMessageAdapter(getBaseContext(), messages);
        lstChatMessages.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        Log.e("Error", e.getMessage());
    }
}
