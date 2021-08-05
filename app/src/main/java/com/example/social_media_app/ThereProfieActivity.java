package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ThereProfieActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    RecyclerView postsRecyclerView;
    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profie);
        postsRecyclerView=findViewById(R.id.postRecyclerview);
        firebaseAuth=FirebaseAuth.getInstance();

        //get uid clicked user to retreivehis posts
        Intent intent=getIntent();
        uid=intent.getStringExtra("uid");
        checkUserStatut();
        loadHisPosts();

    }

    private void loadHisPosts() {
    }
    private void searchHisPosts(){}

    private void checkUserStatut(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is sign in stay here
            // mPrfilTv.setText(user.getEmail());
            uid=user.getUid();
        }
        else{
            //user not sign in go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
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