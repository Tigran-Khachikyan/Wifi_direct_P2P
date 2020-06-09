package com.softvision.wifi_direct_p2p;

import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterRecView extends RecyclerView.Adapter<AdapterRecView.Holder> {

    private List<WifiP2pDevice> devices;
    private OnHolderClickListener holderClickListener;

    AdapterRecView(List<WifiP2pDevice> _devices, OnHolderClickListener onHolderClickListener) {
        devices = _devices;
        holderClickListener = onHolderClickListener;
    }

    void setDeviceNames(List<WifiP2pDevice> _devices) {
        devices = _devices;
        notifyDataSetChanged();
    }

    class Holder extends RecyclerView.ViewHolder {

        private AppCompatTextView number = itemView.findViewById(R.id.tv_number);
        private AppCompatTextView device_name = itemView.findViewById(R.id.tv_device_name);

        void bind(final WifiP2pDevice device, int pos) {
            number.setText(String.valueOf(pos + 1));
            device_name.setText(device.deviceName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holderClickListener.onClick(device);
                }
            });
        }

        Holder(@NonNull View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holder_recycler, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (devices != null)
            holder.bind(devices.get(position), position);
    }

    @Override
    public int getItemCount() {
        return devices != null ? devices.size() : 0;
    }
}
