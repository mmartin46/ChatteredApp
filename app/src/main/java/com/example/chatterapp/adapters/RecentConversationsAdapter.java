package com.example.chatterapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatterapp.databinding.ItemContainerRecentConvoBinding;
import com.example.chatterapp.listeners.ConversionListener;
import com.example.chatterapp.models.ChatMessage;
import com.example.chatterapp.models.User;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final ConversionListener conversionListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessageList, ConversionListener conversionListener) {
        this.chatMessageList = chatMessageList;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConvoBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {


        ItemContainerRecentConvoBinding binding;

        ConversionViewHolder(ItemContainerRecentConvoBinding itemContainerRecentConvoBinding) {
            super(itemContainerRecentConvoBinding.getRoot());
            binding = itemContainerRecentConvoBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.iconProfile.setImageBitmap(getConvoIcon(chatMessage.convoIcon));
            binding.textName.setText(chatMessage.convoName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.convoId;
                user.name = chatMessage.convoName;
                user.icon = chatMessage.convoIcon;
                conversionListener.onConversionClicked(user);
            });
        }

    }

    private Bitmap getConvoIcon(String encodedImage) {
        byte []bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
