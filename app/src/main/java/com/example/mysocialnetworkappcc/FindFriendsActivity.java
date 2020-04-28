package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mysocialnetworkappcc.Models.FindFriends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private EditText searchinputtext;
    private ImageView searchImage;
    private RecyclerView SearchResultList;
    private FirebaseRecyclerAdapter<FindFriends,FindFriendViewHolder> adapter;


    private DatabaseReference allUSerDatabaseRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    String searchboxinput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        mToolbar = (Toolbar) findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(mToolbar);

        SearchResultList = (RecyclerView)findViewById(R.id.search_result_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(linearLayoutManager);


        searchinputtext = findViewById(R.id.findfriend_input);
        searchImage = findViewById(R.id.find_friend_searchButton);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
//        allUSerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        allUSerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchboxinput = searchinputtext.getText().toString();
                Toast.makeText(FindFriendsActivity.this, "Searching\n"+searchboxinput, Toast.LENGTH_SHORT).show();
                //SearchPeopleAndFriend(searchboxinput);
                onStart();
            }
        });

    }

  //  private void SearchPeopleAndFriend(String searchboxinput)

    //{

//        Query query = allUSerDatabaseRef.orderByChild("fullname")
//                .startAt(searchboxinput).endAt(searchboxinput+"\uf8ff");
//        FirebaseRecyclerOptions<FindFriends> options=
//                new FirebaseRecyclerOptions.Builder<FindFriends>()
//                .setQuery(query, new SnapshotParser<FindFriends>() {
//                    @NonNull
//                    @Override
//                    public FindFriends parseSnapshot(@NonNull DataSnapshot snapshot) {
//                        return new FindFriends(snapshot.child("profileimage").getValue().toString()
//                        ,snapshot.child("fullname").getValue().toString()
//                                ,snapshot.child("status").getValue().toString());
//                    }
//                }).build();
//
//
//       FirebaseRecyclerAdapter<FindFriends,FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendViewHolder>(options){
//
//            @NonNull
//            @Override
//            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View view = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.all_users_display_layout , parent ,false);
//
//
//                return new FindFriendViewHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, int position, @NonNull FindFriends model) {
//                holder.setFullname(model.getFullname());
//                holder.setProfileimage(getApplicationContext(),model.getProfileimage());
//                holder.setStatus(model.getStatus());
//            }
//        };
//
//        SearchResultList.setAdapter(adapter);
  //  }

    @Override
    protected void onStart() {
        Query query = allUSerDatabaseRef.orderByChild("fullname")
                .startAt(searchboxinput).endAt(searchboxinput+"\uf8ff");
        FirebaseRecyclerOptions<FindFriends> options=
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(query, new SnapshotParser<FindFriends>() {
                            @NonNull
                            @Override
                            public FindFriends parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new FindFriends(snapshot.child("profileimage").getValue().toString()
                                        ,snapshot.child("fullname").getValue().toString()
                                        ,snapshot.child("status").getValue().toString());
                            }
                        }).build();


        FirebaseRecyclerAdapter<FindFriends,FindFriendViewHolder> adapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendViewHolder>(options){

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout , parent ,false);


                return new FindFriendViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull FindFriends model) {
                holder.setFullname(model.getFullname());
                holder.setProfileimage(getApplicationContext(),model.getProfileimage());
                holder.setStatus(model.getStatus());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        String visit_user_id = getRef(position).getKey();
                        Intent profileIntent =new Intent(getApplicationContext(),PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id",visit_user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };

        SearchResultList.setAdapter(adapter);
        super.onStart();
        adapter.startListening();
    }



    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setProfileimage(Context ctx,String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Glide.with(ctx).load(profileimage).load(profileimage).into(myImage);

        }
        public void setFullname(String fullname)
        {
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setStatus(String status)
        {
            TextView myName = mView.findViewById(R.id.all_users_profile_status);
            myName.setText(status);
        }

    }


}
