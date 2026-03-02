// ServerActivity.java
package com.example.androidsocketpractice;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ServerActivity extends AppCompatActivity {
    EditText etName;
    Button btnCon;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) getSupportActionBar().setTitle("Server");

        etName = findViewById(R.id.etName);
        btnCon = findViewById(R.id.btnCon);

        btnCon.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            Intent i = new Intent(this, ServerConnectedActivity.class);
            i.putExtra("username", name.isEmpty()? "Server" : name);
            startActivity(i);
        });
    }
}
