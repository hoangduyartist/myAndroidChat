package com.example.mychat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//socket.io
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
//socket.io
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    {
        try {
//            mSocket = IO.socket("http://192.168.1.143:3000");
            mSocket = IO.socket("http://duynh-my-chat.herokuapp.com/");
//            mSocket = IO.socket("http://localhost:3000");
        } catch (URISyntaxException e) {}
    }

    private Intent mIntent;
    private TextView mUsernameLb;

    private Button recordBtn, doneBtn, sendBtn;
    private MediaRecorder myRecorder;
    private MediaPlayer myPlayer;
    private String outputFile = null;

    private EditText msgEdt;
    private Button sendMsgBtn;
    private List<String> mMsgList;
    private ListView msgView;
    private ArrayAdapter mAdapter;

    private Button logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        connect socket.io end get data
        mSocket.connect();
        mSocket.on("Server-send-record", onNewRecord);
        mSocket.on("Server-send-message", onNewMessage);
//        connect socket.io end get data

//        get username
        mIntent = getIntent();
        mUsernameLb = (TextView)findViewById(R.id.usernameLb);
        mUsernameLb.setText(mIntent.getStringExtra("username"));

        recordBtn = (Button)findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord(v);
            }
        });
        doneBtn = (Button)findViewById(R.id.done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord(v);
            }
        });
        sendBtn = (Button)findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myChat.3gpp";
                String path = outputFile;
                byte[] sound = FileLocal_To_Byte(path);

                mSocket.emit("Client-send-record",sound);
            }
        });

        msgEdt = (EditText)findViewById(R.id.msgEdtBtn);
        sendMsgBtn = (Button)findViewById(R.id.sendMsgBtn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msgEdt.getText().toString();
                if(msg.equals("")){
                    Toast.makeText(getApplicationContext(), "Please type your message.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSocket.emit("Client-send-message",msg);
                msgEdt.setText("");
            }
        });
        mMsgList = new ArrayList<>();

        mAdapter = new ArrayAdapter(MainActivity.this,android.R.layout.simple_list_item_1,mMsgList);
        msgView = (ListView)findViewById(R.id.msgView);
        msgView.setAdapter(mAdapter);

        logoutBtn = (Button)findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                mSocket.disconnect();
            }
        });

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
//        mSocket.off("new message", onNewMessage);
    }

    private Emitter.Listener onNewRecord = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    byte[] sound;

                    try {
                        sound = (byte[])data.get("content");
                        playMp3FromByte(sound);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("content");
                    } catch (JSONException e) {
                        return;
                    }
                    // add the message to view
                    mMsgList.add(message);
                    mAdapter.notifyDataSetChanged();

                }
            });
        }
    };

    public void startRecord(View view){
        try {
            outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/myChat.3gpp";
            myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myRecorder.setOutputFile(outputFile);

            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Start recording...",
                Toast.LENGTH_SHORT).show();
    }
    public void stopRecord(View view){
        try {
            myRecorder.stop();
            myRecorder.release();
            myRecorder  = null;

            Toast.makeText(getApplicationContext(), "Stop recording...",
                    Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
//convert file to byte
    public byte[] FileLocal_To_Byte(String path){
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
//             TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bytes;
    }
//    play byte file as MP3
    private void playMp3FromByte(byte[] mp3SoundByteArray) {
        try {

            File tempMp3 = File.createTempFile("sound", "mp3", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            MediaPlayer mediaPlayer = new MediaPlayer();

            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
}
