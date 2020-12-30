package com.swayam.uberclone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;

import im.delight.android.location.SimpleLocation;

public class DriverRecyclerViewAdapter extends RecyclerView.Adapter<DriverRecyclerViewAdapter.Holder> {
    private ArrayList<ParseObject> list = new ArrayList<>();
    private SimpleLocation.Point location;
    private OnItemSelectListener listener;

    public void addUser(ParseObject user){
        list.add(user);
        notifyDataSetChanged();
    }

    public void clear(){
        list = new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ParseObject parseObject = list.get(position);
        String username = parseObject.getString("username");
        ParseGeoPoint point = parseObject.getParseGeoPoint("point");
        double lat = point.getLatitude();
        double lon = point.getLongitude();
        //to get near by distance from driver
        if (location != null){
            double distance = SimpleLocation.calculateDistance(location.latitude,location.longitude,lat,lon);
            distance = Math.round(distance);
            distance = distance / 1000;
            holder.distance.setText(""+distance+" KM");
        }
        holder.username.setText(username);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.selectedItem(username,lat,lon);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setDriverLocation(SimpleLocation.Point location) {
        this.location = location;
    }

    class Holder extends RecyclerView.ViewHolder{
        private TextView username;
        private TextView distance;

        public Holder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            distance = itemView.findViewById(R.id.distance);
        }
    }

    public void setOnItemSelectListener(OnItemSelectListener listener){
        this.listener = listener;
    }

    interface OnItemSelectListener {
        void selectedItem(String username,double lat,double lon);
    }
}
