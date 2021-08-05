package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.social_media_app.notifications.APIService;
import com.example.social_media_app.notifications.Client;
import com.example.social_media_app.notifications.Data;
import com.example.social_media_app.notifications.Response;
import com.example.social_media_app.notifications.Sender;
import com.example.social_media_app.notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv,userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;
    private FirebaseAuth firebaseAuth;
    String hisUid;
    String myUid;
    //
    APIService apiService;
    boolean notify=false;
    //
    //
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;
    String hisImage;
    //for checking if use has seen messege or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;
    List<ModelChat> chatList;
    AdapterChat adapterChat;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        recyclerView=findViewById(R.id.chat_recyclerview);
        profileIv=findViewById(R.id.profileIv);
        nameTv=(TextView)findViewById(R.id.nameTv);
        userStatusTv=(TextView)findViewById(R.id.userStatutTv);
        messageEt=(EditText)findViewById(R.id.messageEt);
        sendBtn=(ImageButton)findViewById(R.id.sendBtn);
        //layout linear for Recycler View
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        //create api service
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);


        linearLayoutManager.setStackFromEnd(true);
        //recycler properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseDatabase=FirebaseDatabase.getInstance();
        usersDbRef=firebaseDatabase.getReference("Users");
        //search user to get that user'sinfo
        Query userQuery=usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until requeired if is received
                for (DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name= ""+ ds.child("name").getValue();
                    hisImage= ""+ ds.child("image").getValue();

                    String typingStatus= ""+ ds.child("typingTo").getValue();
                    //check typing status
                    if (typingStatus.equals(myUid)) {

                        userStatusTv.setText("typing...");
                   }else{
                        String onlineStatus=""+ ds.child("onlineStatus").getValue();

                        //get value of onlineStatus
                        if (onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);


                        }else {
                            //convert timestamp to proper time date
                            //convert timestamp to dd/mm/yyyy hh:mm
                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            userStatusTv.setText("Last seen at: "+dateTime);

                        }

                    }




                    //set data
                    nameTv.setText(name);
                    try{
                        //image received ,set it to image view in tool bar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_face_default).into(profileIv);
                    }
                    catch (Exception e){
                        //there is exception getting picture,set default picture
                        Picasso.get().load(R.drawable.ic_face_default).into(profileIv);


                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //click buttton to send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                //get text from edit text
                String message=messageEt.getText().toString().trim();
                //check if text is umpty or not
                if (TextUtils.isEmpty(message)){
                    //text empty
                    Toast.makeText(ChatActivity.this, "cannot send the empty message...", Toast.LENGTH_SHORT).show();

                }else{
                    //text not empty
                    sendMessege(message);

                }
                //reset edit text after sending message
                messageEt.setText("");
            }
        });
        //check edit text change listener
        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length()==0){
                    checkTypingStatut("noOne");
                }else {
                    checkTypingStatut(hisUid);//uid for receiver
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        readMessages();
        seenMessege();
    }

    private void seenMessege() {
        userRefForSeen=FirebaseDatabase.getInstance().getReference("chats");
        seenListener=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hasSeenHashMap=new HashMap<>();
                        hasSeenHashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList=new ArrayList<>();
        DatabaseReference dbref=FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);

                    if(chat.getReceiver().equals(myUid)&& chat.getSender().equals(hisUid)||chat.getReceiver().equals(hisUid)&& chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    adapterChat=new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();
                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessege(final String message) {
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
        String timestamp=String.valueOf(System.currentTimeMillis());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);

        databaseReference.child("Chats").push().setValue(hashMap);
        //reset edit text after sending message

        String msg=message;
        DatabaseReference database=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUsers user=snapshot.getValue(ModelUsers.class);
                if (notify){
                    sentNotification(hisUid,user.getName(),message);
                }
                notify=false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sentNotification(final String hisUid, final String name, final String message) {
        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    Token token=ds.getValue(Token.class);
                    Data data=new Data(myUid,name+":"+message,"New Message", hisUid, R.drawable.ic_face_default);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void checkUserStatut(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is sign in stay here
            // mPrfilTv.setText(user.getEmail());
            myUid=user.getUid();//currently signed in user

        }
        else{
            //user not sign in go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }
    private void checkOnlineStatut(String status){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("onlineStatus",status);
        //update value of onlinestatus of current user
        dbRef.updateChildren(hashMap);




    }
    private void checkTypingStatut(String typing){
        DatabaseReference dbRef=FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("typingTo",typing);
        //update value of onlinestatus of current user
        dbRef.updateChildren(hashMap);




    }


    @Override
    protected void onStart() {
        checkUserStatut();
        checkOnlineStatut("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //get timestamp
        String timestamp=String.valueOf(System.currentTimeMillis());

        //set ofline with last seen time stamp
       checkOnlineStatut(timestamp);
       // checkTypingStatut("noOne");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatut("online");

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide search view ,add post as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatut();
        }
        return super.onOptionsItemSelected(item);
    }
}
