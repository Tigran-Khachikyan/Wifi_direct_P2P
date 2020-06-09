package com.softvision.wifi_direct_p2p;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton btn_wifi_status, btn_discover, btn_send;
    AppCompatTextView tv_message, tv_connection_status;
    private RecyclerView recyclerView;
    private AppCompatEditText et_write_message;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private List<WifiP2pDevice> devices;
    private static final int REQUEST_CODE_WIFI_STATUS = 23;
    private AdapterRecView adapterRecView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        initWifiManagers();
        initBroadcastReceiver();
        initViews();
        initRecyclerView();
        init_btn_wifi_status();
        init_btn_discover();
    }

    private void initWifiManagers() {

        devices = new ArrayList<>();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        if (wifiP2pManager != null)
            channel = wifiP2pManager.initialize(this, getMainLooper(), null);
    }

    private void initBroadcastReceiver() {

        receiver = new WiFiBroadcastDirectReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void initViews() {

        btn_send = findViewById(R.id.btn_send);
        tv_message = findViewById(R.id.tv_read_message);
        tv_connection_status = findViewById(R.id.tv_connection_status);
        et_write_message = findViewById(R.id.et_write_message);

    }

    private void initRecyclerView() {

        recyclerView = findViewById(R.id.recycler_connections);
        adapterRecView = new AdapterRecView(new ArrayList<WifiP2pDevice>(), new OnHolderClickListener() {
            @Override
            public void onClick(final WifiP2pDevice device) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapterRecView);
    }

    private void init_btn_wifi_status() {

        btn_wifi_status = findViewById(R.id.btn_wifi_status);
        if (wifiManager.isWifiEnabled())
            btn_wifi_status.setText("Wifi Off");
        else
            btn_wifi_status.setText("Wifi On");

        btn_wifi_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wifiManager.isWifiEnabled()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                        startActivityForResult(panelIntent, REQUEST_CODE_WIFI_STATUS);
                    } else {
                        wifiManager.setWifiEnabled(false);
                        btn_wifi_status.setText("Wifi On");
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                        startActivityForResult(panelIntent, REQUEST_CODE_WIFI_STATUS);
                    } else {
                        wifiManager.setWifiEnabled(true);
                        btn_wifi_status.setText("Wifi Off");
                    }
                }
            }
        });
    }

    private void init_btn_discover() {

        btn_discover = findViewById(R.id.btn_discover);
        btn_discover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        tv_connection_status.setText("Discovery started");
                    }

                    @Override
                    public void onFailure(int i) {
                        tv_connection_status.setText("Discovery started Failed, reason: " + i);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_WIFI_STATUS) {
            if (wifiManager.isWifiEnabled())
                btn_wifi_status.setText("Wifi Off");
            else btn_wifi_status.setText("Wifi On");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if (!wifiP2pDeviceList.getDeviceList().equals(devices)) {
                devices.clear();
                devices.addAll(wifiP2pDeviceList.getDeviceList());
                adapterRecView.setDeviceNames(devices);
            }
            if (wifiP2pDeviceList.getDeviceList().isEmpty())
                Toast.makeText(MainActivity.this, "NO DEVICE FOUND", Toast.LENGTH_SHORT).show();
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
                tv_connection_status.setText("Host");
            else if(wifiP2pInfo.groupFormed)
                tv_connection_status.setText("Client");

        }
    };


    private void checkPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "ACCESS_COARSE_LOCATION not granted", Toast.LENGTH_SHORT).show();

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    2323);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        } else {
            Toast.makeText(MainActivity.this, "ACCESS_COARSE_LOCATION granted", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 2323 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "ACCESS_COARSE_LOCATION granted", Toast.LENGTH_SHORT).show();
        }
    }
}
