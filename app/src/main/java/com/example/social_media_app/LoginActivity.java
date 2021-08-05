package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN =100 ;
    GoogleSignInClient  mGoogleSignInClient;
    EditText mEmailEt,mPasswordEt;
    TextView notHavaAccountTv,mRecoverPassTv;
    Button mLoginBtn;
    private FirebaseAuth mAuth;
    ProgressDialog pd;
    SignInButton mGoogleLoginBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //action bar and its title
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        mEmailEt=(EditText)findViewById(R.id.emailEt);
        mPasswordEt=(EditText)findViewById(R.id.passwordEt);
        notHavaAccountTv=(TextView) findViewById(R.id.nothave_accountTv);
        mLoginBtn=(Button)findViewById(R.id.loginBtn);

        mRecoverPassTv=(TextView)findViewById(R.id.recoverpassTv);
        mGoogleLoginBtn=findViewById(R.id.ggoogleLoginBtn);
        //configure google sign in
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);
        //initialize the fire baseauth instance
        mAuth = FirebaseAuth.getInstance();
        //text view recover pass word click

        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswoordDialog();
            }
        });
        //button login click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=mEmailEt.getText().toString();
                String password=mPasswordEt.getText().toString();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmailEt.setError("Invalide Email");
                    mEmailEt.setFocusable(true);
                }
                else{
                    LoginUser(email,password);
                }

            }
        });
        //text view not have account click
        notHavaAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                finish();
            }
        });
        pd=new ProgressDialog(this);
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });


    }

    private void showRecoverPasswoordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        //set layout linear layout
        LinearLayout linearLayout=new LinearLayout(this);
        //views to set on dialog
        final EditText emailEt=new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        //sets the min width of a Editview to fit a text of n'M'letters regardless of the actuel text extension and text size
        emailEt.setMinEms(16);
        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email=emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();

    }

    private void beginRecovery(String email) {
        pd.setMessage("Sending email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if(task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //get and show proper error
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void LoginUser(String email, String password) {
        pd.setMessage("Login...");
        pd.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            pd.dismiss();

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                //error,get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                // ...
            }
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = mAuth.getCurrentUser();
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){
                                //get user uid from auth
                                String email=user.getEmail();
                                String uid=user.getUid();
                                //when user is registered store user info in firebase realtime data base too
                                HashMap<Object,String> hashMap= new HashMap<>();
                                //put info in hashmap
                                hashMap.put("email",email);
                                hashMap.put("uid",uid);
                                hashMap.put("name","");//will added later
                                hashMap.put("onlineStatus","online");
                                hashMap.put("typingTo","noOne");


                                hashMap.put("phone","");
                                hashMap.put("image","");
                                hashMap.put("cover","");
                                //firebase database instance
                                FirebaseDatabase database=FirebaseDatabase.getInstance();
                                //path to store user data name "Users"
                                DatabaseReference reference=database.getReference("Users");
                                //put data within hashmap in database
                                reference.child(uid).setValue(hashMap);


                            }








                            //show user email in toast
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            //go to profil activity
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                           // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();

                            //updateUI(null);
                        }

                        // ...
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show error message
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();//go previos activity
        return super.onSupportNavigateUp();
    }


}
