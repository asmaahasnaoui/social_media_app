package com.example.social_media_app;

import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdapterChat extends RecyclerView.Adapter<AdapterChat.MyHolder> {
    private static final int MSG_TYPE_LEFT=0;
    private static final int MSG_TYPE_RIGHT=1;
    Context context;
    List<ModelChat> chatList;
    String imageUri;
    FirebaseUser fUser;

    public AdapterChat(Context context, List<ModelChat> chatList, String imageUri) {
        this.context = context;
        this.chatList = chatList;
        this.imageUri = imageUri;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout;row_chat_left.xml for receiuver row_chat_right.xml for sender
        if(viewType==MSG_TYPE_RIGHT){
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_right,parent,false);
            return new MyHolder(view);


        }else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_chat_left,parent,false);
            return new MyHolder(view);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
        String message=chatList.get(position).getMessage();
        String timestamp=chatList.get(position).getTimestamp();
        //convert timestamp to dd/mm/yyyy hh:mm
        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
        //set data
        holder.messageTv.setText(message);
        holder.timeTv.setText(dateTime);
        try {
            Picasso.get().load(imageUri).into(holder.profileIv);

        }catch (Exception e){

        }
        //click to show delete dialog
        holder.messageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show delete messge confirm dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message?");
                //delete buuton 
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                        
                        
                    }
             
                    
                });
                //cancel delete button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //dialog dissmis
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        //set seen/deleverd sattus of message
        if(position==chatList.size()-1){
            if (chatList.get(position).isSeen()){
            holder.isSeenTv.setText("Seen");}
            else {
                holder.isSeenTv.setText("Delevered");
            }

        }else{
            holder.isSeenTv.setVisibility(View.GONE);

        }

    }

    private void deleteMessage(int position) {
        final String myUID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        //get timestamp of the cliked message
        //compare the timestamp of the clicked message with all message in chats
        //where both values matches delete that message
        String msgTimeStamp=chatList.get(position).getTimestamp();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query=dbRef.orderByChild("timestamp").equalTo(msgTimeStamp);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    //we can do one of two things here
                    //remove the message from chats
                    //set the value of messege "this message was deleted..."
                    //so do whatever you want
                    if (ds.child("sender").getValue().equals(myUID)){
                        //remove the messege from chats

                       // ds.getRef().removeValue();
                        //set value of message "this message was deleted..."
                        HashMap<String,Object>hashMap=new HashMap<>();
                        hashMap.put("message","this message was deleted...");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "message deleted...", Toast.LENGTH_SHORT).show();


                    }else{
                        Toast.makeText(context, "you can delate only your message...", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        //get currently sign in user
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }

    }

    class MyHolder extends RecyclerView.ViewHolder{
        ImageView profileIv;
        TextView messageTv,timeTv,isSeenTv;
        LinearLayout messageLayout;//for click listener to show delete
        
        

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIv=itemView.findViewById(R.id.profileIv);
            messageTv =itemView.findViewById(R.id.messageTv);
            timeTv=itemView.findViewById(R.id.timeTv);
            isSeenTv=itemView.findViewById(R.id.isSeenTv);
            messageLayout=itemView.findViewById(R.id.messageLayout);


        }
    }
}
