package com.swayam.uberclone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DriverRecyclerViewAdapter extends RecyclerView.Adapter<DriverRecyclerViewAdapter.Holder> {
    ArrayList<String> list = new ArrayList<>();

    public void addUser(String user){
        list.add(user);
        notifyDataSetChanged();
    }

    public ArrayList<String> getUsers(){
        return list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.username.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        private TextView username;

        public Holder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
        }
    }
}
