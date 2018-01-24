package com.demo.testchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    static final String APP_ID = "67735";
    static final String AUTH_KEY = "hgADYNJgAZktsrf";
    static final String AUTH_SECRET = "6hUwXU9BQ6DwbrF";
    static final String ACCOUNT_KEY = "mbL3SdEzHTQq6fZtzZJa";
    Button btnLogin, btnSingUp;
    EditText username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeFramework();
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnSingUp = (Button) findViewById(R.id.btnSingUp);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);


    }

    private void initializeFramework() {
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }


    public void singUp(View v) {
        startActivity(new Intent(MainActivity.this, SingUpActivity.class));
    }

    public void login(View v) {
        final String user = this.username.getText().toString();
        final String password = this.password.getText().toString();
        QBUser qbUser = new QBUser(user, password);
        QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Toast.makeText(getApplicationContext(), "Login Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ChatDialogsActivity.class);
                intent.putExtra("user", user);
                intent.putExtra("password", password);
                startActivity(intent);
                finish();// close login activity after logged
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(getApplicationContext(), "Login Failed", Toast.LENGTH_LONG).show();
            }
        });

    }
}
