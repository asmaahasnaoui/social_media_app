package com.example.social_media_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {
    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userDbRef;
    EditText titleEt,descriptionEt;
    ImageView imageIv;
    Button uploadBtn;
    //user info
    String name,email,uid,dp;
    //permession constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    //array of permission to be requested
    String [] camerapermissin;
    String [] storagepermissin;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE=400;
    //imege picked
    Uri image_uri=null;
    //progres  dialog
    ProgressDialog pd;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
         actionBar=getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init array of permission
        camerapermissin=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermissin=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd=new ProgressDialog(this);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUserStatut();
        actionBar.setSubtitle(email);
        //get some info of current user to include in post
        userDbRef= FirebaseDatabase.getInstance().getReference("Users");
        Query query=userDbRef.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    name=""+ds.child("name").getValue();
                    email=""+ds.child("email").getValue();
                    dp=""+ds.child("image").getValue();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        titleEt=findViewById(R.id.pTitleEt);
        descriptionEt=findViewById(R.id.pDescriptionEt);
        imageIv=findViewById(R.id.pImageIv);
        uploadBtn=findViewById(R.id.pUploadBtn);
        //get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show image pick dialogu
                showImagePickDialog();
            }
        });
        //upload btn click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data title ,description from edit text
                String title=titleEt.getText().toString().trim();
                String description=descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title)){
                    Toast.makeText(AddPostActivity.this, "Enter Title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(AddPostActivity.this, "Enter Description", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (image_uri==null){
                    //post without image
                    uploadData(title,description,"noImage");
                }else{
                    //post with image
                    uploadData(title,description,String.valueOf(image_uri));

                }

            }
        });





    }

    private void uploadData(final String title, final String description, String uri) {
        pd.setMessage("publishing post...");
        pd.show();
        final String timeStamp=String.valueOf(System.currentTimeMillis());
        String filePathAndName="Posts/"+"post_"+timeStamp;
        if(!uri.equals("noImage")){
            //post with image
            StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
            ref.putFile(Uri.parse(uri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image is upload to firebase storage,now get it's uri
                            Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            String downloadUri=uriTask.getResult().toString();
                            if(uriTask.isSuccessful()){
                                //uri is received upload post to firebase datbase
                                HashMap<Object,String> hashMap=new HashMap<>();
                                //put post info
                                hashMap.put("uid",uid);
                                hashMap.put("uName",name);
                                hashMap.put("uEmail",email);
                                hashMap.put("uDp",dp);
                                hashMap.put("pId",timeStamp);
                                hashMap.put("pTitle",title);
                                hashMap.put("pDescr",description);
                                hashMap.put("pImage",downloadUri);
                                hashMap.put("pTime",timeStamp);
                                //path to store post data
                                DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
                                //put data in this ref
                                ref.child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //added in datbase
                                                pd.dismiss();
                                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                                                //reset views
                                                titleEt.setText("");
                                                descriptionEt.setText("");
                                                imageIv.setImageURI(null);
                                                image_uri=null;


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed adding post in database
                                        pd.dismiss();
                                        Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                });





                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

        }
        else {
            //post without image
            HashMap<Object, String> hashMap = new HashMap<>();
            //put post info
            hashMap.put("uid", uid);
            hashMap.put("uName", name);
            hashMap.put("uEmail", email);
            hashMap.put("uDp", dp);
            hashMap.put("pId", timeStamp);
            hashMap.put("pTitle", title);
            hashMap.put("pDescr", description);
            hashMap.put("pImage", "noImage");
            hashMap.put("pTime", timeStamp);
            //path to store post data
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            //put data in this ref
            ref.child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //added in datbase
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                            //reset views
                            titleEt.setText("");
                            descriptionEt.setText("");
                            imageIv.setImageURI(null);
                            image_uri=null;

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //failed adding post in database
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });




    }


        }



    private void showImagePickDialog() {
        //option camera gallery to show in dialog
        String option[]={"Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Chose image from");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which==0){
                    //camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }
                     }
                if(which==1){
                    //gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }

                }


            }
        });
        builder.create().show();
    }
    private void pickFromCamera() {
        //intent of picking image from device camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Desc");

        //put image uri
        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        //intent to start camera
        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_REQUEST_CODE);

    }
    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }
    private boolean checkStoragePermission(){
        //check if storage permission is ennable or not
        //return true if ennable
        //return false if not
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;

    }
    private void requestStoragePermission(){
       ActivityCompat.requestPermissions(this,storagepermissin,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission(){
        //check if camera permission is ennable or not
        //return true if ennable
        //return false if not
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);

        return result && result1;

    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,camerapermissin,CAMERA_REQUEST_CODE);

    }


    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatut();
    }

    private void checkUserStatut(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is sign in stay here
            email=user.getEmail();
            uid=user.getUid();

        }
        else{
            //user not sign in go to main activity
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatut();
        }

        return super.onOptionsItemSelected(item);
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //this methode called when user press allow or deny from permission request dialog
        //here we will handle permission cases (allowed or deny)
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera ,first check if camera and storag permission allowed or not
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        //permission enable
                        pickFromCamera();
                    }else{
                        //permission deny
                        Toast.makeText(this, "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                    }


                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    //picking from gallery ,first check if storag permission allowed or not
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if( writeStorageAccepted){
                        //permission enable
                        pickFromGallery();
                    }else{
                        //permission deny
                        Toast.makeText(this, "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }


                }
            }break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this methode will be called after picking image from camera or gallery

        if(resultCode == RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery,get uri of image
                image_uri=data.getData();
                imageIv.setImageURI(image_uri);


            }
            if(requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                //image is picked from camera ,get uri of image
                imageIv.setImageURI(image_uri);



            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
