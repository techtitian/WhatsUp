package com.rafaelfloressouza.whatsup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private ArrayList<User> userList;

    public UserListAdapter(ArrayList<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, null, false);

        // Making sure that the view has has a width that matches the parent and height that wraps content.
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        // Getting the recycler View
        return new UserListViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, final int position) {

        // Setting the text view's name and phone to the the corresponding user present in the userList.
        holder.mName.setText(userList.get(position).getName());
        holder.mPhone.setText(userList.get(position).getPhone());

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
                final String currentUserId = FirebaseAuth.getInstance().getUid();
                final String otherUserId = userList.get(position).getUid();

                Query query = FirebaseDatabase.getInstance().getReference().child("user").child(currentUserId).child("chat");
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {

                        boolean otherUserFound = false;
                        String chatID = "";

                        // Looking if other user has a chat
                        if (snapshot.exists()) {

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.getValue().equals(otherUserId)) {
                                    otherUserFound = true;
                                    chatID = dataSnapshot.getKey();
                                    break;
                                }
                            }
                        }

                        final String new_chat_id = chatID;


                        if (!otherUserFound) {

                            // No chat exists between the users.
                            Toast.makeText(v.getContext(), "Creating New Chat", Toast.LENGTH_SHORT).show();
                            FirebaseDatabase.getInstance().getReference().child("user").child(currentUserId).child("chat").child(key).setValue(otherUserId);
                            FirebaseDatabase.getInstance().getReference().child("user").child(otherUserId).child("chat").child(key).setValue(currentUserId);

                        } else {

                            // Users already have a chat, so we go to their activity
                            Toast.makeText(v.getContext(), "Going to Chat", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent intent = new Intent(v.getContext(), ChatActivity.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString("chatID", new_chat_id);
                                        intent.putExtras(bundle);
                                        v.getContext().startActivity(intent);
                                    } catch (Exception e) {
                                        Log.d("WhatsUp", "error: " + e.toString());
                                    }
                                }
                            }, 1000);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.userList.size();
    }

    public class UserListViewHolder extends RecyclerView.ViewHolder {

        public TextView mName, mPhone;
        public LinearLayout mLayout;

        public UserListViewHolder(View view) {
            super(view);
            mName = view.findViewById(R.id.user_name);
            mPhone = view.findViewById(R.id.user_phone);
            mLayout = view.findViewById(R.id.user_item_layout);
        }
    }
}
