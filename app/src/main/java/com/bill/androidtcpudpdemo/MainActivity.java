package com.bill.androidtcpudpdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btn_tp_server;
    Button btn_tp_client;
    Button btn_udp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_tp_server = findViewById(R.id.btn_tp_server);
        btn_tp_client = findViewById(R.id.btn_tp_client);
        btn_udp = findViewById(R.id.btn_udp);
    }

    public void button(View v){
        switch (v.getId()){
            case R.id.btn_tp_server:
                Intent i = new Intent(MainActivity.this,TCPServerActivity.class);
                startActivity(i);
                break;
            case R.id.btn_tp_client:
                i = new Intent(MainActivity.this,TCPClientActivity.class);
                startActivity(i);

                break;
            case R.id.btn_udp:
                i = new Intent(MainActivity.this,UDPActivity.class);
                startActivity(i);
                break;
        }
    }

}
