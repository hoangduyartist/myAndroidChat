package com.example.mychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class AuthActivity extends AppCompatActivity {

    private Button loginBtn;

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mIntent = new Intent(this,MainActivity.class);

        loginBtn = (Button)findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mIntent.putExtra()
                startActivity(mIntent);
            }
        });

    }
}
