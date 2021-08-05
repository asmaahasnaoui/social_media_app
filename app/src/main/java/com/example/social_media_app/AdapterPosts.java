package com.example.social_media_app;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterPosts extends RecyclerView.Adapter<AdapterPosts.MyHolder> {
    Context context;
    List<ModelPost> postList;

    public AdapterPosts(Context context, List<ModelPost> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view= LayoutInflater.from(context).inflate(R.layout.row_posts,parent,false);

        return new MyHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        //get data
        final String uid=postList.get(position).getUid();
        String uEmail=postList.get(position).getuEmail();
        String uName=postList.get(position).getuName();
        String uDp=postList.get(position).getuDp();
        String pId=postList.get(position).getpId();
        String pTitle=postList.get(position).getpTitle();
        String pDescription=postList.get(position).getpDescr();
        String pImage=postList.get(position).getpImage();
        String pTimeStamp=postList.get(position).getpTime();

        //convert timestamp to dd/mm/yyyy hh:mm
        Calendar calender=Calendar.getInstance(Locale.getDefault());
        calender.setTimeInMillis(Long.parseLong(pTimeStamp));
        String pTime= DateFormat.format("dd/MM/yyyy hh:mm aa",calender).toString();

        //set data

        holder.uNameTv.setText(uName);
        holder.pTimeTv.setText(pTime);
        holder.pTitleTv.setText(pTitle);
        holder.pDescriptionTv.setText(pDescription);

        //set user dp
        try {
            Picasso.get().load(uDp).placeholder(R.drawable.ic_face_default).into(holder.uPictureIv);

        }catch (Exception e){

        }
        //set post image
        //if ther is no image
        if (pImage.equals("noImage")){
            //hide image view
            holder.pImageIv.setVisibility(View.GONE);


        }
        else{
            try {
                Picasso.get().load(pImage).into(holder.pImageIv);

            }catch (Exception e){


            }


        }
        //handle button click
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "More", Toast.LENGTH_SHORT).show();
            }
        });
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Like", Toast.LENGTH_SHORT).show();
            }
        });
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Comment", Toast.LENGTH_SHORT).show();
            }
        });
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share", Toast.LENGTH_SHORT).show();
            }
        });
        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //click to go to there profile with uid is of clicked user
                Intent intent=new Intent(context,ThereProfieActivity.class);
                intent.putExtra("uid",uid);
                context.startActivity(intent);

            }
        });









    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{
        //views from row_post
        ImageView uPictureIv,pImageIv;
        TextView uNameTv,pTimeTv,pTitleTv,pDescriptionTv,pLikesTv;
        ImageButton moreBtn;
        Button likeBtn,commentBtn,shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            //init views
            uPictureIv=itemView.findViewById(R.id.uPictureIv);
        pImageIv=itemView.findViewById(R.id.pImageIv);
            uNameTv=itemView.findViewById(R.id.uNameTv);
            pTimeTv=itemView.findViewById(R.id.pTimeTv);
            pTitleTv=itemView.findViewById(R.id.pTitleTv);
            pDescriptionTv=itemView.findViewById(R.id.pDescriptionTv);
            pLikesTv=itemView.findViewById(R.id.pLikeTv);
            moreBtn=itemView.findViewById(R.id.moreBtn);
            likeBtn=itemView.findViewById(R.id.likeBtn);
            commentBtn=itemView.findViewById(R.id.commentBtn);
            shareBtn=itemView.findViewById(R.id.shareBtn);
            profileLayout=itemView.findViewById(R.id.profileLayout);










        }
    }
}
