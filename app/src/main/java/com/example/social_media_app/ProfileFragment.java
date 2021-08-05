package com.example.social_media_app;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.text.method.ArrowKeyMovementMethod.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    //views from xml
    ImageView avatarIv,coverIv;
    FloatingActionButton fab;
    TextView nameTv,emailTv,phoneTv;

    RecyclerView postsRecyclerView;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ProgressDialog pd;
    //permession constants
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_GALLERY_CODE=300;
    private static final int IMAGE_PICK_CAMERA_REQUEST_CODE=400;
    //array of permission to be requested
    String camerapermissin[];
    String storagepermissin[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;


    //uri of pickedimage
    Uri image_uri;
    //for checking profil or covert photo
    String profilOrCovertPhoto;
    //storage
    StorageReference storageReference;
    //path where images of user profiland cover will be stored
    String storagePath="Users_Profile_Cover_Imgs/";




    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        //init firebase
        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference= FirebaseStorage.getInstance().getReference();


        //init array of permission
        camerapermissin=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagepermissin=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        pd=new ProgressDialog(getActivity());

        //init view

        avatarIv=view.findViewById(R.id.avatarIv);
        coverIv=view.findViewById(R.id.coverIv);
        fab=view.findViewById(R.id.fab);

        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);

        postsRecyclerView=view.findViewById(R.id.postRecyclerview);

        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
       /* query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check until required data
                for (DataSnapshot ds:snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    String email=""+ds.child("email").getValue();
                    String phone=""+ds.child("phone").getValue();
                    String image=""+ds.child("image").getValue();
                    String cover=""+ds.child("cover").getValue();
                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //if image received then set
                        Picasso.get().load(image).into(avatarIv);


                    }catch (Exception e){
                        //if ther is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_face_default).into(avatarIv);

                    }
                    try {
                        //if image received then set
                        Picasso.get().load(cover).into(coverIv);


                    }catch (Exception e){
                        //if ther is any exception while getting image then set default


                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
          fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           }
        });
          checkUserStatut();
        //  loadMyPosts();

        return view;
    }

    private void loadMyPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        //show newest post first ,for this load from last
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);
        //init posts list
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        // query to load posts
        Query query=ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myposts=ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myposts);
                    //adapter
                    adapterPosts=new AdapterPosts(getActivity(),postList);
                    //set this adapter to recycler view
                    postsRecyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void searcMyPosts(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        //show newest post first ,for this load from last
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);
        //init posts list
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Posts");
        // query to load posts
        Query query=ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelPost myposts=ds.getValue(ModelPost.class);
                    if(myposts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase())||
                    myposts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        //add to list
                        postList.add(myposts);

                    }

                    //add to list
                    postList.add(myposts);
                    //adapter
                    adapterPosts=new AdapterPosts(getActivity(),postList);
                    //set this adapter to recycler view
                    postsRecyclerView.setAdapter(adapterPosts);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }

    private boolean checkStoragePermission(){
        //check if storage permission is ennable or not
        //return true if ennable
        //return false if not
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;

    }
   private void requestStoragePermission(){
        requestPermissions(storagepermissin,STORAGE_REQUEST_CODE);

    }
    private boolean checkCameraPermission(){
        //check if storage permission is ennable or not
        //return true if ennable
        //return false if not
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;

    }
    private void requestCameraPermission(){
        requestPermissions(camerapermissin,CAMERA_REQUEST_CODE);

    }

    private void showEditProfilDialog() {
        //option to show in dialog
        String options[]={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone"};
        //aler Dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //setTitile
        builder.setTitle("Choose Action");
        //set item to Dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //edit profil picture
                    pd.setMessage("Updating Profile Picture");
                    profilOrCovertPhoto="image";
                    showImagePicDialog();

                }else  if(which==1){
                    //edit cover phot
                    pd.setMessage("Updating Cover Photo");
                    profilOrCovertPhoto="cover";
                    showImagePicDialog();

                }
                else  if(which==2){
                    //edit Name
                    pd.setMessage("Updating Name");
                    //calling methode and pass key "name"as parametre to updateit's valuein database
                    showNamePhoneUpdateDialogue("name");

                }
                else  if(which==3){
                    //edit phone
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialogue("phone");


                }
            }
        });
        //dreate and show dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialogue(final String key) {
        //custom dailogue
        final AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);//e.g update name or update phone
        //set layout of dialog
        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        //add edit text
        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter"+key);//eg enter Name or enter phone
        linearLayout.addView(editText);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        //add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit Text
                String value=editText.getText().toString().trim();
                //validate if user has entered somthing or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object>result=new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //update dismiss progress
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failled ,dissmis progress,get and show error message
                            pd.dismiss();
                            Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();


                        }
                    });



                }else{
                    Toast.makeText(getActivity(), "Please enter"+key, Toast.LENGTH_SHORT).show();


                }


            }
        });

        //add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog containing options camera and gallery to pick the image
        //option to show in dialog
        String options[]={"Camera","Gallery"};
        //aler Dialog
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        //setTitile
        builder.setTitle("Pick Image From");
        //set item to Dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    //Camera clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }


                }else  if(which==1){
                    //Gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }


                }

            }
        });
        //dreate and show dialog
        builder.create().show();

    }

    @Override
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
                    Toast.makeText(getActivity(), "Please enable camera & storage permission", Toast.LENGTH_SHORT).show();
                }


            }

        }
        break;
        case STORAGE_REQUEST_CODE:{
            if(grantResults.length>0){
                //picking from gallery ,first check if storag permission allowed or not
                boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                if( writeStorageAccepted){
                    //permission enable
                    pickFromGallery();
                }else{
                    //permission deny
                    Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                }


            }
        }break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

   private void pickFromCamera() {
        //intent of picking image from device camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
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

   @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //this methode will be called after picking image from camera or gallery

        if(resultCode == RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery,get uri of image
                image_uri=data.getData();
                uploadProfileCovertPhoto(image_uri);

            }
            if(requestCode==IMAGE_PICK_CAMERA_REQUEST_CODE){
                //image is picked from camera ,get uri of image
                uploadProfileCovertPhoto(image_uri);



            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCovertPhoto(Uri uri) {
        //show progress
        pd.show();
        //path and name of image to be stored in firebase storage
        //e.g Users_profil_cover_Imgs/image_e125425GVG.jpg
        //e.g Users_profil_cover_Imgs/cover_e178542klVG.jpg

        String filePathAndName=storagePath+""+profilOrCovertPhoto+"_"+user.getUid();
        StorageReference storageReference2nd=storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is upload to storage ,now getit's url and store in user's data base
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                    Uri downloadUri=uriTask.getResult();
                    //check if image is upload or not
                    if(uriTask.isSuccessful()){
                        //image uploadded
                        //add update url in user's database
                        HashMap<String,Object>results=new HashMap<>();
                        results.put(profilOrCovertPhoto,downloadUri.toString());
                        databaseReference.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //url in database of user is added succesfully
                                //dissmiss progress bar
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                //error adding url in database of user
                                //dissmissprogress bar
                                pd.dismiss();
                                Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();



                            }
                        });


                    }else{
                        //error
                        pd.dismiss();
                        Toast.makeText(getActivity(), "some error occured", Toast.LENGTH_SHORT).show();
                    }




            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there were some errors ,get and show error message dismissprogress dialogue
                pd.dismiss();
                Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void checkUserStatut(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is sign in stay here
            // mPrfilTv.setText(user.getEmail());
            uid=user.getUid();
        }
        else{
            //user not sign in go to main activity
            startActivity(new Intent(getActivity(),MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//toshow menu option in fragment
        super.onCreate(savedInstanceState);
    }
    //inflate option  menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        MenuItem item=menu.findItem(R.id.action_search);

        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when
                if(!TextUtils.isEmpty(query)){
                    //search
                    searcMyPosts(query);


                }else {
                   // loadMyPosts();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    //search
                    searcMyPosts(newText);


                }else {
                    //loadMyPosts();

                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }
    //handle menu item CLICK

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id=item.getItemId();
        if(id==R.id.action_logout){
            firebaseAuth.signOut();
           checkUserStatut();
        }
        if(id==R.id.action_add){
            startActivity(new Intent(getActivity(),AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}
