package com.bill.androidtcpudpdemo;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TCPServerActivity extends AppCompatActivity {
    private Thread mThread = null;
    private TextView tvMessages;
    private EditText etMessage;
    private int SERVER_PORT = 5500; //傳送埠號
    private String message;
    private BufferedReader reader;
    private Socket socket;
    private String tmp;
    private PrintWriter out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tcp_server);
        TextView tvIP = findViewById(R.id.tvIP);
        TextView tvPort = findViewById(R.id.tvPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);
        try {
            String SERVER_IP = getLocalIpAddress();
            tvIP.setText("IP: " + SERVER_IP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        mThread = new Thread(new serverThread());
        mThread.start();

        tvMessages.setText("Not connected");
        tvPort.setText("Port: " + String.valueOf(SERVER_PORT));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = etMessage.getText().toString();
                if (!message.isEmpty()) {
                    new Thread(new SendData(message)).start();
                }
            }
        });
    }


    class serverThread implements Runnable {
        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
                try {
                    socket = serverSocket.accept();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() { tvMessages.setText("Connected\n");
                        }
                    });
                    // 取得網路輸入串流
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //取得網路輸出串流
                    out = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream())),
                            true);
                    while ((tmp = reader.readLine()) != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.append("收 "+socket.getInetAddress().getHostAddress()
                                        + ": " + tmp + "\n");
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class SendData implements Runnable {
        private String message;
        SendData(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            out.println(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("發 "+socket.getLocalAddress().getHostAddress()
                            +": " + message + "\n");
                }
            });
        }
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
        mThread.interrupt();
    }
}