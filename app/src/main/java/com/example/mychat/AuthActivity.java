package com.example.mychat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class AuthActivity extends AppCompatActivity {
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://duynh-my-chat.herokuapp.com/");
//            mSocket = IO.socket("http://192.168.1.144:3000");
        } catch (URISyntaxException e) {
        }
    }

    private EditText userEdt;
    private Button loginBtn;
    private TextView loginResTxt;

    private EditText userRegEdt;
    private Button regBtn;
    private TextView regResTxt;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        Toast.makeText(getApplicationContext(), "Create AuthActivity.", Toast.LENGTH_SHORT).show();

        mSocket.connect();
        mSocket.on("user-login-res", onNewLoginRes);
        mSocket.on("user-register-res", onNewRegRes);

        userEdt = (EditText) findViewById(R.id.userEdt);
        loginResTxt = (TextView)findViewById(R.id.loginResTxt);
        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.connect();
                String username = userEdt.getText().toString();
                if (username.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please input user-name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSocket.emit("user-login",username);
            }
        });

        userRegEdt = (EditText) findViewById(R.id.userRegEdt);
        regResTxt = (TextView)findViewById(R.id.regResTxt);
        regBtn = (Button) findViewById(R.id.regBtn);
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSocket.connect();
                String username = userRegEdt.getText().toString();
                if (username.equals("")) {
                    Toast.makeText(getApplicationContext(), "Please input user-name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSocket.emit("user-register",username);
            }
        });

    }
    private Emitter.Listener onNewLoginRes = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String loginRes;
                    String stt;
                    try {
                        loginRes = data.getString("content");
                        stt = data.getString("status");
                    } catch (JSONException e) {
                        return;
                    }
                    if(stt.equals("true")){
                        mIntent = new Intent(AuthActivity.this, MainActivity.class);
                        mIntent.putExtra("username",userEdt.getText().toString());
                        startActivity(mIntent);
                        loginResTxt.setText("");
                    }else {
                        loginResTxt.setText(loginRes);
                    }

                }
            });
        }
    };

    private Emitter.Listener onNewRegRes = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String regRes;
                    String stt;
                    try {
                        regRes = data.getString("content");
                        stt = data.getString("status");
                    } catch (JSONException e) {
                        return;
                    }
                    if(stt.equals("true")){
                        regResTxt.setTextColor(Color.parseColor("#00FF33"));
                        regResTxt.setText(regRes);
                    }else {
                        regResTxt.setTextColor(Color.parseColor("#D81B21"));
                        regResTxt.setText(regRes);
                    }

                }
            });
        }
    };
}
