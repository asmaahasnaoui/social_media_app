package com.example.social_media_app;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUsers>usersList;
    private FirebaseAuth firebaseAuth;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        recyclerView=view.findViewById(R.id.users_recycler_view);
        //set recycler view properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //init user list
        usersList=new ArrayList<>();
        //get All user
        getAllUser();
        return view;
    }

    private void getAllUser() {
        //get current user
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get path of data base named "Users containing user info
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    //get all user except currently signed in user
                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);
                    }
                    //adapter
                    adapterUsers=new AdapterUsers(getActivity(),usersList);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void searchUsers(final String query) {
        //get current user
        final FirebaseUser fUser= FirebaseAuth.getInstance().getCurrentUser();
        //get path of data base named "Users containing user info
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers modelUsers=ds.getValue(ModelUsers.class);
                    //get all searched user except currently signed in user
                    if (!modelUsers.getUid().equals(fUser.getUid())){
                        if (modelUsers.getName().toLowerCase().contains(query.toLowerCase())||
                        modelUsers.getEmail().toLowerCase().contains(query.toLowerCase())){
                            usersList.add(modelUsers);
                        }

                    }
                    //adapter
                    adapterUsers=new AdapterUsers(getActivity(),usersList);
                    //refrech adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);

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

        //search view
        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView= (SearchView)MenuItemCompat.getActionView(item);
        //Search listent
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //called when user press search button from keyboard
                //if search query is not empety then search
                if (!TextUtils.isEmpty(query.trim())){
                    //search text contain text search it
                    searchUsers(query);

                }
                else{
                    //search text empty ,get all users
                    getAllUser();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //called whenever user press any single letter
                if (!TextUtils.isEmpty(newText.trim())){
                    //search text contain text search it
                    searchUsers(newText);

                }
                else{
                    //search text empty ,get all users
                    getAllUser();
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

        return super.onOptionsItemSelected(item);
    }

}
