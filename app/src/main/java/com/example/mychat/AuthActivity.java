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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class AuthActivity extends AppCompatActivity {

    private Socket mSocket;

    private EditText userEdt;
    private Button loginBtn;
    private TextView loginResTxt;

    private EditText userRegEdt;
    private Button regBtn;
    private TextView regResTxt;

    private Intent mIntent;

    public Socket getmSocket(){
//        if (mSocket.connected()){
//            return mSocket;
//        }
//        mSocket.connected();
        return mSocket;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        try {
            mSocket = IO.socket("http://duynh-my-chat.herokuapp.com/");
//            mSocket = IO.socket("http://192.168.1.145:3000");
            mSocket.connect();
        } catch (URISyntaxException e) {
            Toast.makeText(getApplicationContext(), "Connect to socket-server failed.", Toast.LENGTH_SHORT).show();
        }
        mSocket.on("user-login-res", onNewLoginRes);
        mSocket.on("user-register-res", onNewRegRes);
//        mSocket.on("Server-send-allMessage", onFetchAllMessage);

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
    private Emitter.Listener onFetchAllMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];

                    JSONArray content;
                    ArrayList<String> testArr = new ArrayList<String>();
                    try {
                        String c1 = data.getJSONArray("content").get(0).toString();
                        content = (JSONArray) data.getJSONArray("content");
                        for(int i=0; i<content.length(); i++){
                            testArr.add(content.get(i).toString());
                            Toast.makeText(getApplicationContext(), "authAct "+content.get(i).toString(), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        return;
                    }

                }
            });
        }
    };
}
