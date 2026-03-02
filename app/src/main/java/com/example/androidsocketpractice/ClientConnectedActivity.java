// ClientConnectedActivity.java
package com.example.androidsocketpractice;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ClientConnectedActivity extends AppCompatActivity
        implements ClientSocketThread.ClientCallbacks {

    TextView tvHi;
    EditText etMessage;
    Button btnSend, btnLeave;
    RecyclerView recyclerChat;
    ChatAdapter adapter;
    ArrayList<ChatMessage> chatList = new ArrayList<>();
    ClientSocketThread socketThread;

    String ip; int port; String username;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_connected);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setTitle("Client Connected");

        tvHi = findViewById(R.id.tvHi);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnLeave = findViewById(R.id.btnLeave);
        recyclerChat = findViewById(R.id.recyclerChat);

        adapter = new ChatAdapter(chatList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);

        ip = getIntent().getStringExtra("ip");
        port = getIntent().getIntExtra("port", 0);
        username = getIntent().getStringExtra("username");
        tvHi.setText("Hi, " + username);

        btnSend.setEnabled(false); // 連上前禁用

        if (socketThread == null) {
            socketThread = new ClientSocketThread(ip, port, username, this);
            socketThread.start();
        }

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;
            socketThread.send(msg);
            etMessage.setText("");
        });

        btnLeave.setOnClickListener(v -> {
            if (socketThread != null) socketThread.disconnect();
            finish();
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (socketThread != null) socketThread.disconnect();
        socketThread = null;
    }

    // === Callbacks ===
    @Override public void onConnected() {
        runOnUiThread(() -> {
            btnSend.setEnabled(true);
            chatList.add(new ChatMessage("[系統] 已連線到 " + ip + ":" + port));
            adapter.notifyItemInserted(chatList.size()-1);
            recyclerChat.scrollToPosition(chatList.size()-1);
        });
    }

    @Override public void onMessage(String line) {
        runOnUiThread(() -> {
            chatList.add(new ChatMessage(line));
            adapter.notifyItemInserted(chatList.size()-1);
            recyclerChat.scrollToPosition(chatList.size()-1);
        });
    }

    @Override public void onError(String err) {
        runOnUiThread(() -> {
            Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
        });
    }

    @Override public void onDisconnected(String reason) {
        runOnUiThread(() -> {
            btnSend.setEnabled(false);
            chatList.add(new ChatMessage("[系統] " + (reason==null? "連線已中斷" : reason)));
            adapter.notifyItemInserted(chatList.size()-1);
            recyclerChat.scrollToPosition(chatList.size()-1);
        });
    }
}
