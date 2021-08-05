package com.example.social_media_app;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {
    private FirebaseAuth firebaseAuth;



    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chat_list, container, false);


        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }
    private void checkUserStatut(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is sign in stay here
            // mPrfilTv.setText(user.getEmail());
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
        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add).setVisible(false);



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
