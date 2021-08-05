package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText mEmailEt,mPasswordEt;
    Button mRegisterbtn;
    //progress bar to display while registring user
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    TextView mHaveAccountTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //action bar and its title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Create Account");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        mEmailEt=(EditText)findViewById(R.id.emailEt);
        mPasswordEt=(EditText)findViewById(R.id.passwordEt);
        mRegisterbtn=(Button) findViewById(R.id.registerBtn);
        mHaveAccountTv=(TextView)findViewById(R.id.have_accountTv);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Registring User ...");
        mAuth = FirebaseAuth.getInstance();


        mRegisterbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEt.getText().toString().trim();
                String password=mPasswordEt.getText().toString().trim();
                //validation
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    //set error and focus to email edit text
                    mEmailEt.setError("invalide Email");
                    mEmailEt.setFocusable(true);

                }
                else if(password.length()<6) {
                    //set error and focus to password edit text
                    mPasswordEt.setError("password length at least 6 caracters");
                    mPasswordEt.setFocusable(true);

                }
                else{
                    registerUser(email,password);
                }


            }
        });
        //handl textview click Listner
        mHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

}

    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialogue and start register activity
                            progressDialog.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            //get user uid from auth
                            String email=user.getEmail();
                            String uid=user.getUid();
                            //when user is registered store user info in firebase realtime data base too
                            HashMap<Object,String>hashMap= new HashMap<>();
                            //put info in hashmap
                            hashMap.put("email",email);
                            hashMap.put("uid",uid);
                            hashMap.put("name","");//will added later
                            hashMap.put("phone","");
                            hashMap.put("onlineStatus","online");
                            hashMap.put("typingTo","noOne");


                            hashMap.put("image","");
                            hashMap.put("cover","");
                            //firebase database instance
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            //path to store user data name "Users"
                            DatabaseReference reference=database.getReference("Users");
                            //put data within hashmap in database
                            reference.child(uid).setValue(hashMap);


                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                             startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                             finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();

                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error,dismiss progress dialogue and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previos activity
        return super.onSupportNavigateUp();
    }
}