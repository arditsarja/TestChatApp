package com.demo.testchatapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bhargavms.dotloader.DotLoader;
import com.demo.testchatapp.Adapter.ChatMessageAdapter;
import com.demo.testchatapp.Common.Common;
import com.demo.testchatapp.Holder.QBChatMessageHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBChatDialogParticipantListener;
import com.quickblox.chat.listeners.QBChatDialogTypingListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBPresence;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.chat.request.QBMessageUpdateBuilder;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestUpdateBuilder;
import com.squareup.picasso.Picasso;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ChatMessageActivity extends AppCompatActivity implements QBChatDialogMessageListener {

    QBChatDialog qbChatDialog;
    ListView lstChatMessages;
    ImageButton submitButton;
    EditText editContent;


    Toolbar chatMessageGroupToolbar;
    ChatMessageAdapter adapter;

    //Update Online User

    ImageView imgOnlineCount, dialogAvatar;
    TextView txtOnlineCount;

    int contextMenuIndexClicked = -1;
    boolean isEditMode = false;
    QBChatMessage editMessage;

    //Typing
    DotLoader dotLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);

        initView();
        initChatDialogs();
        retriveMessage();

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
        });
        AlertDialog alertDialog = builder.create();
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
        if (!editContent.getText().toString().trim().isEmpty())
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

        if (qbChatDialog.getPhoto() != null && !qbChatDialog.getPhoto().equals("null")) {
            QBContent.getFile(Integer.parseInt(qbChatDialog.getPhoto())).performAsync(new QBEntityCallback<QBFile>() {
                @Override
                public void onSuccess(QBFile qbFile, Bundle bundle) {
                    Picasso.with(getBaseContext()).load(qbFile.getPublicUrl())
                            .resize(50, 50)
                            .centerCrop().into(dialogAvatar);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e("Error picture", e.getMessage());
                }
            });
        }


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

        //Add typing listener
        registerTypingForChatDialog(qbChatDialog);


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
        QBChatDialogParticipantListener participantListener = new QBChatDialogParticipantListener() {
            @Override
            public void processPresence(String dialogId, QBPresence qbPresence) {
                if (dialogId == qbChatDialog.getDialogId()) {
                    QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
                        @Override
                        public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                            try {
                                qbChatDialog.getOnlineUsers();
                                TextDrawable.IBuilder builder = TextDrawable.builder()
                                        .beginConfig()
                                        .withBorder(4)
                                        .endConfig()
                                        .round();
                                TextDrawable online = builder.build("" + qbChatDialog.getOnlineUsers().size(),
                                        qbChatDialog.getOnlineUsers().size() > 1 ? Color.GREEN : Color.RED
                                );
                                imgOnlineCount.setImageDrawable(online);
                                txtOnlineCount.setText("" + qbChatDialog.getOnlineUsers().size()
                                        + "/" + qbChatDialog.getOccupants().size() + " online");

                            } catch (XMPPException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(QBResponseException e) {

                        }
                    });


                }
            }
        };
        qbChatDialog.addParticipantListener(participantListener);
        qbChatDialog.addMessageListener(this);


        chatMessageGroupToolbar.setTitle(qbChatDialog.getName());
        setSupportActionBar(chatMessageGroupToolbar);


    }

    private void registerTypingForChatDialog(QBChatDialog qbChatDialog) {
        QBChatDialogTypingListener typingListener = new QBChatDialogTypingListener() {
            @Override
            public void processUserIsTyping(String dialogID, Integer integer) {
                if (dotLoader.getVisibility() != View.VISIBLE)
                    dotLoader.setVisibility(View.VISIBLE);
            }

            @Override
            public void processUserStopTyping(String dialogID, Integer integer) {
                if (dotLoader.getVisibility() != View.INVISIBLE)
                    dotLoader.setVisibility(View.INVISIBLE);
            }
        };
        qbChatDialog.addIsTypingListener(typingListener);

    }

    private void initView() {

        dotLoader = findViewById(R.id.dot_loader);

        lstChatMessages = findViewById(R.id.list_of_message);
        submitButton = findViewById(R.id.send_button);
        editContent = findViewById(R.id.edit_content);
        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                String hi = "";
//                try {
//                    qbChatDialog.sendIsTypingNotification();
//                } catch (XMPPException e) {
//                    e.printStackTrace();
//                } catch (SmackException.NotConnectedException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (count != 0)
                    try {
                        qbChatDialog.sendIsTypingNotification();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                else
                    try {
                        qbChatDialog.sendStopTypingNotification();
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String hi = s.toString();
                String hisd = s.toString();
//                try {
//                    qbChatDialog.sendStopTypingNotification();
//                } catch (XMPPException e) {
//                    e.printStackTrace();
//                } catch (SmackException.NotConnectedException e) {
//                    e.printStackTrace();
//                }
            }
        });


        chatMessageGroupToolbar = findViewById(R.id.chat_message_toolbar);
        imgOnlineCount = findViewById(R.id.img_online_count);
        dialogAvatar = findViewById(R.id.dialog_avatar);
        dialogAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent selectImage = new Intent();
                selectImage.setType("image/*");
                selectImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(selectImage, "Select Picture"), Common.SELECT_PICTURE);

            }
        });

        txtOnlineCount = findViewById(R.id.txt_online_count);

        //add context menu
        registerForContextMenu(lstChatMessages);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Common.SELECT_PICTURE) {
                final ProgressDialog dialog = new ProgressDialog(ChatMessageActivity.this);
                dialog.setMessage("Please wait...");
                dialog.setCancelable(false);
                dialog.show();

                try {
                    InputStream is = getContentResolver().openInputStream(data.getData());
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    File file = new File(Environment.getExternalStorageDirectory() + "/image.png");
                    FileOutputStream fileOut = new FileOutputStream(file);
                    fileOut.write(baos.toByteArray());
                    fileOut.flush();
                    fileOut.close();

                    int imageSizeKb = (int) file.length() / 1024;
                    if (imageSizeKb >= 100 * 1024) {
                        Toast.makeText(this, "File size is to big", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Upload File
                    QBContent.uploadFileTask(file, true, null).performAsync(new QBEntityCallback<QBFile>() {
                        @Override
                        public void onSuccess(QBFile qbFile, Bundle bundle) {
                            qbChatDialog.setPhoto(qbFile.getId().toString());
                            QBRequestUpdateBuilder requestBuilder = new QBRequestUpdateBuilder();
                            QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                                    dialog.dismiss();
                                    dialogAvatar.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    dialog.dismiss();
                                    Toast.makeText(ChatMessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.e("ERROR", e.getMessage());
                        }
                    });

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Picasso.with(getApplicationContext()).load(data.getData())
                        .resize(20, 20)
                        .into(dialogAvatar);
            }
        }
    }

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
