// ServerConnectedActivity.java
package com.example.androidsocketpractice;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ServerConnectedActivity extends AppCompatActivity
        implements ServerSocketThread.ServerEvents {

    private static final int SERVER_PORT = 7100;

    TextView tvHi;
    EditText etMessage;
    Button btnSend, btnLeave;
    RecyclerView recyclerChat;
    ChatAdapter adapter;
    ArrayList<ChatMessage> chatList = new ArrayList<>();
    ServerSocketThread serverThread;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connected);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setTitle("Server Connected");

        tvHi = findViewById(R.id.tvHi);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnLeave = findViewById(R.id.btnLeave);
        recyclerChat = findViewById(R.id.recyclerChat);

        adapter = new ChatAdapter(chatList);
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(adapter);

        String username = getIntent().getStringExtra("username");
        tvHi.setText("Hi, " + (username==null? "Server" : username));

        // 啟動 Server
        serverThread = new ServerSocketThread(getApplicationContext(), SERVER_PORT, this);
        serverThread.start();

        btnSend.setOnClickListener(v -> {
            String msg = etMessage.getText().toString().trim();
            if (msg.isEmpty()) return;

            try {
                if (serverThread == null || !serverThread.isAlive()) {
                    Toast.makeText(this, "Server 尚未啟動", Toast.LENGTH_SHORT).show();
                    return;
                }
                serverThread.broadcast("Server:" + msg);     // 廣播
                chatList.add(new ChatMessage("Server: " + msg)); // 本地顯示
                adapter.notifyItemInserted(chatList.size() - 1);
                recyclerChat.scrollToPosition(chatList.size() - 1);
                etMessage.setText("");
            } catch (Exception e) {
                Toast.makeText(this, "送出失敗：" + e.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
            }
        });


        btnLeave.setOnClickListener(v -> {
            if (serverThread != null) {
                serverThread.broadcast("[系統] Server 已關閉");   // 先告知
                serverThread.stopServer();
            }
            finish();
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (serverThread != null) {
            serverThread.broadcast("[系統] Server 已關閉");   // 保險再送一次
            serverThread.stopServer();
        }
    }

    @Override
    public void onServerMessage(String msg) {
        runOnUiThread(() -> {
            chatList.add(new ChatMessage(msg));                 // 顯示 client 來的訊息
            adapter.notifyItemInserted(chatList.size()-1);
            recyclerChat.scrollToPosition(chatList.size()-1);
        });
    }

    @Override
    public void onServerStarted(String ip) {
        runOnUiThread(() -> {
            ChatMessage m = new ChatMessage("[系統] Server started (" + ip + ":" + SERVER_PORT + ")");
            chatList.add(m);
            adapter.notifyItemInserted(chatList.size()-1);
            recyclerChat.scrollToPosition(chatList.size()-1);
        });
    }
}
