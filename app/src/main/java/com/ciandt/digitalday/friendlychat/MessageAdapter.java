package com.ciandt.digitalday.friendlychat;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Collection;
import java.util.List;

/**
 * Created by felipearimateia on 01/09/16.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder>{

    private final Context context;
    private final List<Message> items;
    private final LayoutInflater layoutInflater;

    public MessageAdapter(Context context, List<Message> items) {
        this.context = context;
        this.items = items;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRoot = layoutInflater.inflate(R.layout.item_message, parent, false);
        ViewHolder viewHolder = new ViewHolder(viewRoot);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {
        Message message = getItem(position);
        holder.messageTextView.setText(message.getText());
        holder.messengerTextView.setText(message.getName());

        if (message.getPhotoUrl() == null) {
            holder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(context,
                    R.drawable.ic_account_circle_black_36dp));
        } else {
            Glide.with(context)
                    .load(message.getPhotoUrl())
                    .into(holder.messengerImageView);
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public Message getItem(int position) {
        return items.get(position);
    }

    public void addAll(final Collection<Message> messages) {
        items.addAll(messages);
        notifyDataSetChanged();
    }

    public void add(final Message message) {
        items.add(message);
        notifyItemInserted(getItemCount() - 1);
    }

    public void insert(final Message message, int index) {
        items.add(index, message);
        notifyItemInserted(index);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView messageTextView;
        TextView messengerTextView;
        ImageView messengerImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            messageTextView = (TextView)itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView)itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (ImageView)itemView.findViewById(R.id.messengerImageView);

        }
    }
}
