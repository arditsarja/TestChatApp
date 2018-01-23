package com.demo.testchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SingUpActivity extends AppCompatActivity {
    public Button btnCancel, btnSingUp;
    public EditText username, password, fullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);
        registerSeesion();

        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSingUp = (Button) findViewById(R.id.btnSingUp);

        username = (EditText) findViewById(R.id.singUpUsername);
        password = (EditText) findViewById(R.id.singUpPassword);
        fullName = (EditText) findViewById(R.id.singUpFullName);

    }

    private void registerSeesion() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {
                Log.e("Error", e.getMessage());
            }
        });
    }

    public void register(View v) {
        String username = this.username.getText().toString();
        String password = this.password.getText().toString();
        String fullName = this.fullName.getText().toString();

        QBUser qbUser = new QBUser(username, password);
        qbUser.setFullName(fullName);
        QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Toast.makeText(getApplicationContext(), "Registered with success!", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                e.printStackTrace();

                for (String error : e.getErrors()) {
                    if (error.contains("password")) {
                        setHint(error);
                    }
                }
            }
        });
    }

    public void cancel(View v) {
        startActivity(new Intent(SingUpActivity.this, MainActivity.class));
    }

    public void setHint(String error) {
        this.password.setHint(this.password.getHint() + " " + error);
    }
}
