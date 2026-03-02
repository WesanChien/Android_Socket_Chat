// ChatAdapter.java（最簡單的單行文字）
package com.example.androidsocketpractice;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final List<ChatMessage> data;
    public ChatAdapter(List<ChatMessage> data) { this.data = data; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tv;
        VH(TextView tv) { super(tv); this.tv = tv; }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView tv = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new VH(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tv.setText(data.get(position).text);
    }

    @Override
    public int getItemCount() { return data.size(); }
}
