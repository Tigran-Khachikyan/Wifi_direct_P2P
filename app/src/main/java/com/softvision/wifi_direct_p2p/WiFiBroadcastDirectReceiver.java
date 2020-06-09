package com.softvision.wifi_direct_p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

public class WiFiBroadcastDirectReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity activity;

    public WiFiBroadcastDirectReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null) {
            String action = intent.getAction();
            switch (action) {

                case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        Toast.makeText(context, "WIFI IS ON", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "WIFI IS OFF", Toast.LENGTH_SHORT).show();
                    }
                    break;


                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    if (wifiP2pManager != null)
                        wifiP2pManager.requestPeers(channel, activity.peerListListener);
                    break;


                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    if(wifiP2pManager!=null){
                        NetworkInfo networkInfo = intent.getParcelableExtra(wifiP2pManager.EXTRA_NETWORK_INFO);
                        if(networkInfo.isConnected())
                            wifiP2pManager.requestConnectionInfo(channel,activity.connectionInfoListener);
                        else
                            activity.tv_connection_status.setText("Disconnected.");
                    }
                    break;
                case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:
                    break;
            }
        }

    }
}
