package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mysocialnetworkappcc.Models.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UserRef;
    private FirebaseAuth mAuth;
    private FirebaseRecyclerAdapter friendsadapter;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllfriends();

    }

    private void DisplayAllfriends()

    {
        Query query = FriendsRef;
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query, new SnapshotParser<Friends>() {
                    @NonNull
                    @Override
                    public Friends parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new Friends(snapshot.child("date").getValue().toString());
                    }
                }).build();
            friendsadapter = new FirebaseRecyclerAdapter<Friends, friendsViewHolder>(options){

                @NonNull
                @Override
                public friendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.all_users_display_layout, parent, false);

                    return new friendsViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull final friendsViewHolder holder, int position, @NonNull Friends model) {
                    holder.setDate(model.getDate());

                    final  String usersIDs = getRef(position).getKey();
                    UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                         if (dataSnapshot.exists()){
                             final  String userName = dataSnapshot.child("fullname").getValue().toString();
                             final  String profileImage = dataSnapshot.child("profileimage").getValue().toString();

                             holder.setFullname(userName);
                             holder.setProfileimage(getApplicationContext(),profileImage);

                             holder.mView.setOnClickListener(new View.OnClickListener() {
                                 @Override
                                 public void onClick(View v) {
                                     CharSequence options[] = new CharSequence[]
                                             {
                                               userName + "'s Profile" ,
                                               "Send Message"
                                             };
                                     AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                     builder.setTitle("Select Option");
                                     builder.setItems(options, new DialogInterface.OnClickListener() {
                                         @Override
                                         public void onClick(DialogInterface dialog, int which)
                                         {
                                             if (which == 0)
                                             {
                                                 Intent profileintent = new Intent(getApplicationContext(),PersonProfileActivity.class);
                                                 profileintent.putExtra("visit_user_id",usersIDs);
                                                 startActivity(profileintent);
                                             }
                                             if (which==1)
                                             {
                                                 Intent chatintent = new Intent(getApplicationContext(),ChatActivity.class);
                                                 chatintent.putExtra("visit_user_id",usersIDs);
                                                 chatintent.putExtra("userName",userName);
                                                 startActivity(chatintent);

                                             }

                                         }
                                     });
                                     builder.show();
                                 }
                             });



                         }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            };
            myFriendList.setAdapter(friendsadapter);

    }


    public static class friendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public friendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Glide.with(ctx).load(profileimage).load(profileimage).into(myImage);

        }
        public void setFullname(String fullname)
        {
            TextView myName = mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date)
        {
            TextView friendsDate = mView.findViewById(R.id.all_users_profile_status);
            friendsDate.setText(date);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        friendsadapter.startListening();
    }
}
