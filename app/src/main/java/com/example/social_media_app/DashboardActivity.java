package com.example.social_media_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.social_media_app.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    String mUID;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        //action bar and its title
         actionBar=getSupportActionBar();
        actionBar.setTitle("Profil ");
        firebaseAuth = FirebaseAuth.getInstance();
       // mPrfilTv=(TextView)findViewById(R.id.profilTv);
        //botton navigation
        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);
        HomeFragment fragment1=new HomeFragment();
        FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();

        ft1.replace(R.id.content,fragment1,"");
        ft1.commit();
        checkUserStatut();
        updateToken(FirebaseInstanceId.getInstance().getToken());



    }

    @Override
    protected void onResume() {
        checkUserStatut();
        super.onResume();
    }

    public void updateToken(String token){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        ref.child(mUID).setValue(mToken);

    }

    private final BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //handl item click
            switch (item.getItemId()) {
                case R.id.nav_home:
                    actionBar.setTitle("Home");//change action bar title
                    HomeFragment fragment1 = new HomeFragment();
                    FragmentTransaction ft1=getSupportFragmentManager().beginTransaction();
                    ft1.replace(R.id.content, fragment1, "");
                    ft1.commit();
                    //home fragment transaction
                    return true;
                case R.id.nav_profil:
                    actionBar.setTitle("Profil");//change action bar title
                    //profil fragment transaction
                    ProfileFragment fragment2 = new ProfileFragment();
                    FragmentTransaction ft2=getSupportFragmentManager().beginTransaction();
                    ft2.replace(R.id.content, fragment2, "");
                    ft2.commit();

                    return true;
                case R.id.nav_users:
                    //users fragment transaction
                    actionBar.setTitle("Users");//change action bar title
                    UsersFragment fragment3 = new UsersFragment();
                    FragmentTransaction ft3=getSupportFragmentManager().beginTransaction();
                    ft3.replace(R.id.content,fragment3,"");
                    ft3.commit();

                    return true;
                case R.id.nav_chat:
                    //users fragment transaction
                    actionBar.setTitle("Users");//change action bar title
                    ChatListFragment fragment4 = new ChatListFragment();
                    FragmentTransaction ft4=getSupportFragmentManager().beginTransaction();
                    ft4.replace(R.id.content,fragment4,"");
                    ft4.commit();

                    return true;
            }
            return false;
        }
    };
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void checkUserStatut(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is sign in stay here
           // mPrfilTv.setText(user.getEmail());
            mUID=user.getUid();
            SharedPreferences sp=getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();
        }
        else{
            //user not sign in go to main activity
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatut();
        super.onStart();
    }

}
