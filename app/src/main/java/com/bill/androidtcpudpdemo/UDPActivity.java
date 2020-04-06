package com.bill.androidtcpudpdemo;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UDPActivity extends AppCompatActivity {

    private final static String TAG = "UDPActivity";
    private TextView tvMessages;
    private EditText etMessage;
    private int port = 8085; //傳送埠號
    private String message;

    private boolean listenStatus = true;  //接收執行緒的迴圈標識
    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;
    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.udp);

        TextView ipTextView = findViewById(R.id.ipTextView);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        try {
            ipTextView.setText(getLocalIpAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = etMessage.getText().toString();
                if (!message.isEmpty()) {
                    new Thread(new SendData(message)).start();
                }
            }
        });
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        new UdpReceiveThread().start();


    }

    /**
     *   UDP 接收資料
     */
    public class UdpReceiveThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                receiveSocket = new DatagramSocket(port);
                receiveSocket.setBroadcast(true);
                while (listenStatus) {
                    byte[] buf = new byte[1024];
                    final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    receiveSocket.receive(packet);
                    final String data = new String(packet.getData()).trim();
                    Log.i(TAG, "Packet received from: "+packet.getAddress().getHostAddress()+
                            "data: " + data);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMessages.append(packet.getAddress().getHostAddress()+
                                    ":" + data + "\n");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *   UDP 發送資料
     **/
    class SendData implements Runnable {
        private String message;
        SendData(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            try {
                sendSocket = new DatagramSocket();
                sendSocket.setBroadcast(true);
                byte[] buf = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(buf, buf.length,
                        getBroadcastAddress(), port);
                sendSocket.send(sendPacket);
                Log.i(TAG,"Broadcast packet sent to: " +
                        getBroadcastAddress().getHostAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    InetAddress getBroadcastAddress() throws IOException {
        assert wifiManager != null;
        int broadcast = (wifiManager.getDhcpInfo().ipAddress &
                wifiManager.getDhcpInfo().netmask) | ~ wifiManager.getDhcpInfo().netmask;
        byte[] quads = new byte[4];
        for (int i = 0; i < 4; i++)
            quads[i] = (byte) ((broadcast >> i * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private String getLocalIpAddress() throws UnknownHostException {
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ipInt).array()).getHostAddress();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        listenStatus = false;
        if(sendSocket != null){
            sendSocket.close();
            sendSocket = null;
        }
        if(receiveSocket != null){
            receiveSocket.close();
            receiveSocket = null;
        }
    }

}
