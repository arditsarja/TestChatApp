package com.demo.testchatapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.demo.testchatapp.Adapter.ListUsersAdapter;
import com.demo.testchatapp.Common.Common;
import com.demo.testchatapp.Holder.QBUsersHolder;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.request.QBDialogRequestBuilder;
import com.quickblox.chat.utils.DialogUtils;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.List;

public class ListUsersActivity extends AppCompatActivity {

    ListView listView;
    Button button;

    String mode = "";
    QBChatDialog qbChatDialog;
    List<QBUser> userAdd = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users);

        mode = getIntent().getStringExtra(Common.UPDATE_MODE);
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(Common.UPDATE_DIALOG_EXTRA);

        listView = findViewById(R.id.lstUsers);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        button = findViewById(R.id.brn_create_chat);

        if (mode == null && qbChatDialog == null)
            retriveAllUser();
        else {
            if (mode.equals(Common.UPDATE_ADD_MODE))
                loadListAvaibleUser();
            else if (mode.equals(Common.UPDATE_REMOVE_MODE))
                loadListUserInGroup();
        }

    }

    public void createChat(View view) {

        if (mode == null) {

            int countChoise = listView.getCount();
            if (listView.getCheckedItemCount() == 1)
                createPrivateChat(listView.getCheckedItemPositions());
            else if (listView.getCheckedItemCount() > 1)
                createGroupChat(listView.getCheckedItemPositions());
            else
                Toast.makeText(getBaseContext(), "Select a friend to chat", Toast.LENGTH_LONG).show();
        } else if (mode.equals(Common.UPDATE_ADD_MODE) && qbChatDialog != null) {
            if (userAdd.size() > 0) {
                QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                int cntChoise = listView.getCount();
                SparseBooleanArray checkItemPosition = listView.getCheckedItemPositions();
                for (int i = 0; i < cntChoise; i++) {
                    if (checkItemPosition.get(i)) {
                        QBUser user = (QBUser) listView.getItemAtPosition(i);
                        requestBuilder.addUsers(user);
                    }
                }
                // Call services
                QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Add user success!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "Remove user unsuccess!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        } else if (mode.equals(Common.UPDATE_REMOVE_MODE) && qbChatDialog != null) {
            if (userAdd.size() > 0) {
                QBDialogRequestBuilder requestBuilder = new QBDialogRequestBuilder();
                int cntChoise = listView.getCount();
                SparseBooleanArray checkItemPosition = listView.getCheckedItemPositions();
                for (int i = 0; i < cntChoise; i++) {
                    if (checkItemPosition.get(i)) {
                        QBUser user = (QBUser) listView.getItemAtPosition(i);
                        requestBuilder.removeUsers(user);
                    }
                }
                QBRestChatService.updateGroupChatDialog(qbChatDialog, requestBuilder).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        Toast.makeText(getBaseContext(), "Remove user success!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(getBaseContext(), "Remove user unsuccess!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }



    }

    private void loadListUserInGroup() {
        button.setText("Remove user");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                List<QBUser> userAlredyExist = QBUsersHolder.getInstance().getUsersByIDS(qbChatDialog.getOccupants());
                ArrayList<QBUser> users = new ArrayList<>(userAlredyExist);

                ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), users);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                userAdd = users;

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadListAvaibleUser() {
        button.setText("Add User");
        QBRestChatService.getChatDialogById(qbChatDialog.getDialogId()).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                ArrayList<QBUser> userList = QBUsersHolder.getInstance().getAllUsers();
                List<QBUser> usersAlreayInChat = QBUsersHolder.getInstance().getUsersByIDS(qbChatDialog.getOccupants());
                for (QBUser user : usersAlreayInChat)
                    userList.remove(user);

                if (userList.size() > -0) {
                    ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), userList);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    userAdd = userList;
                }

            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(ListUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void createPrivateChat(SparseBooleanArray checkedItemPositions) {

        final ProgressDialog mDialog = new ProgressDialog(ListUsersActivity.this);
        mDialog.setMessage("Please waiting ...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();


        int countChoise = listView.getCount();

        for (int i = 0; i < countChoise; i++) {
            if (checkedItemPositions.get(i)) {
                final QBUser user = (QBUser) listView.getItemAtPosition(i);
                QBChatDialog chatDialog = DialogUtils.buildPrivateDialog(user.getId());
                QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        mDialog.dismiss();
                        Toast.makeText(getBaseContext(), "Create chat dialog succes", Toast.LENGTH_SHORT).show();
                        QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

                        QBChatMessage qbChatMessage = new QBChatMessage();
                        qbChatMessage.setRecipientId(user.getId());
                        qbChatMessage.setBody(qbChatDialog.getDialogId());
                        try {
                            qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.e("Error", e.getMessage());
                    }
                });
            }
        }
    }

    private void createGroupChat(SparseBooleanArray checkedItemPositions) {


        final ProgressDialog mDialog = new ProgressDialog(ListUsersActivity.this);
        mDialog.setMessage("Please waiting ...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();

        int countChoise = listView.getCount();
        ArrayList<Integer> ocupantIdsList = new ArrayList<>();
        for (int i = 0; i < countChoise; i++) {
            if (checkedItemPositions.get(i)) {
                QBUser user = (QBUser) (listView.getItemAtPosition(i));
                ocupantIdsList.add(user.getId());
            }
        }
        QBChatDialog chatDialog = new QBChatDialog();
        chatDialog.setName(Common.createChateDialogName(ocupantIdsList));
        chatDialog.setType(QBDialogType.GROUP);
        chatDialog.setOccupantsIds(ocupantIdsList);

        QBRestChatService.createChatDialog(chatDialog).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                mDialog.dismiss();
                Toast.makeText(getBaseContext(), "Create chat dialog succes", Toast.LENGTH_SHORT).show();
                //Send system message to recipient id user
                QBSystemMessagesManager qbSystemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

                QBChatMessage qbChatMessage = new QBChatMessage();
                qbChatMessage.setBody(qbChatDialog.getDialogId());
                for (int id : qbChatDialog.getOccupants()) {
                    qbChatMessage.setRecipientId(id);
                    try {
                        qbSystemMessagesManager.sendSystemMessage(qbChatMessage);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Error", e.getMessage());
            }
        });

    }


    private void retriveAllUser() {

        QBUsers.getUsers(null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
//                ArrayList<QBUser> qbUserWithotCurrent = new ArrayList<>(qbUsers);
//                qbUserWithotCurrent.remove(QBChatService.getInstance().getUser());

                QBUsersHolder.getInstance().putUsers(qbUsers);

                ArrayList<QBUser> qbUserWithotCurrent = new ArrayList<>();
                for (QBUser user : qbUsers) {
                    if (!user.getLogin().equals(QBChatService.getInstance().getUser().getLogin()))
                        qbUserWithotCurrent.add(user);
                }

                ListUsersAdapter adapter = new ListUsersAdapter(getBaseContext(), qbUserWithotCurrent);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Error", e.getMessage());
            }
        });
    }
}
