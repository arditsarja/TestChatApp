package com.demo.testchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.demo.testchatapp.Common.Common;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfile extends AppCompatActivity {
    EditText edtPassword, edtOldPassword, edtFullName, edtEmail, edtPhone;
    Button updateUser, cancel;
    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        toolbar = findViewById(R.id.user_update_toolbar);
        toolbar.setTitle("Profile Update");
        setSupportActionBar(toolbar);
        initUserProfile();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserProfile.super.onBackPressed();
//                startActivity(new Intent(UserProfile.this,ChatDialogsActivity.class));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_update_logout:
                logOut();
                break;
            default:
                break;
        }
        return true;
    }

    private void logOut() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//remove all previws activity
                startActivity(intent);
                Toast.makeText(UserProfile.this, "Logged Out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void updateUser(View v) {
        QBUser user = QBChatService.getInstance().getUser();
        if (!Common.isNullOrEmptyString(edtOldPassword.getText().toString()))
            user.setOldPassword(edtOldPassword.getText().toString());
        if (!Common.isNullOrEmptyString(edtPassword.getText().toString()))
            user.setPassword(edtPassword.getText().toString());
        if (!Common.isNullOrEmptyString(edtFullName.getText().toString()))
            user.setFullName(edtFullName.getText().toString());
        if (!Common.isNullOrEmptyString(edtEmail.getText().toString()))
            user.setEmail(edtEmail.getText().toString());
        if (!Common.isNullOrEmptyString(edtPhone.getText().toString()))
            user.setPhone(edtPhone.getText().toString());
        final ProgressDialog progressDialog = new ProgressDialog(UserProfile.this);
        progressDialog.setMessage("Please wait..");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "User " + qbUser.getLogin() + " is updated!", Toast.LENGTH_LONG).show();


            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error in update!", Toast.LENGTH_SHORT).show();
                Log.e("ERROR", e.getMessage());

            }
        });
    }

    private void initUserProfile() {
        edtOldPassword = findViewById(R.id.update_edt_old_password);
        edtPassword = findViewById(R.id.update_edt_password);
        edtFullName = findViewById(R.id.update_edt_full_name);
        edtEmail = findViewById(R.id.update_edt_email);
        edtPhone = findViewById(R.id.update_edt_phonee);
        updateUser = findViewById(R.id.update_user_btn_update);
        cancel = findViewById(R.id.update_user_btn_cancel);

    }
}
