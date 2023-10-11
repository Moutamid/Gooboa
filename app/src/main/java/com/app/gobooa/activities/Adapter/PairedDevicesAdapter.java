package com.app.gobooa.activities.Adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.gobooa.R;
import com.app.gobooa.activities.utils.DeviceModel;
import com.mazenrashed.printooth.Printooth;

import java.util.List;

public class PairedDevicesAdapter extends RecyclerView.Adapter<PairedDevicesAdapter.GalleryPhotosViewHolder> {


    Context ctx;
    List<DeviceModel> categoryModelList;

    public PairedDevicesAdapter(Context ctx, List<DeviceModel> categoryModelList) {
        this.ctx = ctx;
        this.categoryModelList = categoryModelList;
    }

    @NonNull
    @Override
    public GalleryPhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.device_adapter_view, parent, false);
        return new GalleryPhotosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryPhotosViewHolder holder, final int position) {
        DeviceModel categoryNameModel = categoryModelList.get(position);

        holder.name.setText(categoryNameModel.name);
        holder.address.setText(categoryNameModel.address);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Printooth.INSTANCE.setPrinter(categoryNameModel.name, categoryNameModel.address);
                Toast.makeText(ctx, "Device is paired", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModelList.size();
    }

    public class GalleryPhotosViewHolder extends RecyclerView.ViewHolder {
        TextView name, address;
        public GalleryPhotosViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvDeviceName);
            address = itemView.findViewById(R.id.tvDeviceAddress);

        }
    }
}
