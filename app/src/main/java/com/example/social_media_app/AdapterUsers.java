package com.example.social_media_app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder> {
    Context context;
    List<ModelUsers>userList;
    //construcut

    public AdapterUsers(Context context, List<ModelUsers> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_users,parent,false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String hisUid=userList.get(position).getUid();
        String userImage=userList.get(position).getImage();
        String userName=userList.get(position).getName();
        final String userEmail=userList.get(position).getEmail();
        //set Data
        holder.mNameTv.setText(userName);
        holder.mEmailTv.setText(userEmail);
        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_face_default).into(holder.mAvatarIv);


        }catch (Exception e){

        }
        //handle item Click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //show dialog
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            //profile cheked
                            //click to go to there profile with uid is of clicked user
                            Intent intent=new Intent(context,ThereProfieActivity.class);
                            intent.putExtra("uid",hisUid);
                            context.startActivity(intent);

                        }
                        if(which==1){
                            //chat cheked
                            Intent intent=new Intent(context,ChatActivity.class);
                            intent.putExtra("hisUid",hisUid);
                            context.startActivity(intent);
                        }

                    }
                });
                builder.create().show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //view holder class
class MyHolder extends RecyclerView.ViewHolder{
    ImageView mAvatarIv;
    TextView mNameTv,mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //initViews
            mAvatarIv=itemView.findViewById(R.id.avatarIv);
            mNameTv=itemView.findViewById(R.id.NametTv);
            mEmailTv=itemView.findViewById(R.id.EmailTv);


        }
    }
}
