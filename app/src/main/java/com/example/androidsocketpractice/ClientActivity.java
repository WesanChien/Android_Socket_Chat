// ClientActivity.java（只負責拿使用者輸入→跳頁）
package com.example.androidsocketpractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ClientActivity extends AppCompatActivity {
    EditText etIp, etPort, etName;
    Button btnCon;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setTitle("Client");

        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etName = findViewById(R.id.etName);
        btnCon = findViewById(R.id.btnCon);

        btnCon.setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            String p = etPort.getText().toString().trim();
            String name = etName.getText().toString().trim();
            if (ip.isEmpty() || p.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "請完整填寫 IP / Port / Name", Toast.LENGTH_SHORT).show();
                return;
            }
            int port;
            try { port = Integer.parseInt(p); }
            catch (NumberFormatException e) { Toast.makeText(this, "Port 必須為數字", Toast.LENGTH_SHORT).show(); return; }

            Intent i = new Intent(this, ClientConnectedActivity.class);
            i.putExtra("ip", ip);
            i.putExtra("port", port);
            i.putExtra("username", name);
            startActivity(i);
        });
    }
}
