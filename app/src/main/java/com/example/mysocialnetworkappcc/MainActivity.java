package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mysocialnetworkappcc.Models.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mtoolbaar;
    private CircleImageView NavProfileImage;
    private ImageButton AddNewPostButton;
    private TextView NavProfileUserName;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, mheaderRef, PostsRef, LikesRef;
    private FirebaseUser user;
    private FirebaseRecyclerAdapter adapter;
    private String currentUserID,Visit_user_ID;
    private Boolean LikeChecker = false;
    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        mheaderRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mtoolbaar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbaar);
        getSupportActionBar().setTitle(R.string.home);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        //actionBarDrawerToggle.getDrawerArrowDrawable().setColor(R.color.white);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView)findViewById(R.id.navigation_view);


        postList = (RecyclerView)findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);




        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);
        //include hader
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);
        AddNewPostButton = (ImageButton)findViewById(R.id.add_new_post_button);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                UserMenuSelector(menuItem);

                return false;
            }
        });


        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Glide.with(getApplicationContext()).load(image).into(NavProfileImage);
                    }else {
                        SendUsertoSetpActivity();
                    }
                    if (dataSnapshot.hasChild("fullname"))
                    {
                        NavProfileUserName.setText(dataSnapshot.child("fullname").getValue().toString());
                    }else {
                        SendUsertoSetpActivity();
                    }

                }else {
                    SendUsertoSetpActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),PostActivity.class));

            }
        });

        DisplayAllUsersPosts();
    }



    private void DisplayAllUsersPosts()
    {

        Query SortPostsinDescendingOrder = PostsRef.orderByChild("counter");
        //Query query = PostsRef;
        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>().
                        setQuery(SortPostsinDescendingOrder, new SnapshotParser<Posts>() {
                            @NonNull
                            @Override
                            public Posts parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Posts(snapshot.child("uid").getValue().toString(),
                                        snapshot.child("time").getValue().toString(),
                                        snapshot.child("date").getValue().toString(),
                                        snapshot.child("postimage").getValue().toString(),
                                        snapshot.child("description").getValue().toString(),
                                        snapshot.child("profileimage").getValue().toString(),
                                        snapshot.child("fullname").getValue().toString()
                                        );
                            }
                        }).build();
        adapter = new FirebaseRecyclerAdapter<Posts,PostsViewHolder>(options){

            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);

                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model) {

                final String PostKey = getRef(position).getKey();
               // Visit_user_ID = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey).child("uid").getKey().toString();

                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setFullname(model.getFullname());
                holder.setTime(model.getTime());
                holder.setProfileimage(getApplicationContext(), model.getProfileimage());
                holder.setPostimage(getApplicationContext(),model.getPostimage());

                holder.setLikeButtonStatus(PostKey);
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent = new Intent(getApplicationContext(),ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickPostIntent);
                    }
                });
                holder.likes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LikeChecker= true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (LikeChecker.equals(true))
                                {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false;

                                    }else {
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false;

                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
                holder.comments.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent clickcommenttIntent = new Intent(getApplicationContext(),CommentsActivity.class);
                        clickcommenttIntent.putExtra("PostKey",PostKey);
                        startActivity(clickcommenttIntent);
                    }
                });
//                holder.profile_image.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent prflIntent = new Intent(getApplicationContext(),PersonProfileActivity.class);
//                        prflIntent.putExtra("visit_user_id",Visit_user_ID);
//                        startActivity(prflIntent);
//                    }
//                });

            }
        };
        postList.setAdapter(adapter);

    }


    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder
    {
        public LinearLayout root;
        ImageView likes,comments;
        CircleImageView profile_image;
        View mView;
        TextView DisplaynoLikes;
        int countLikes;
        String curretUserId;
        DatabaseReference LikesRef;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            likes = mView.findViewById(R.id.likeButton);
            comments = mView.findViewById(R.id.comments_btn);
            DisplaynoLikes = mView.findViewById(R.id.display_no_likes);

            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }
        public void setLikeButtonStatus(final String postKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                 if (dataSnapshot.child(postKey).hasChild(currentUserID))
                 {
                     countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                     likes.setImageResource(R.drawable.like);
                     DisplaynoLikes.setText((Integer.toString(countLikes)+(" Likes")));
                 }else
                     {
                         countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                         likes.setImageResource(R.drawable.dislikepng);
                         DisplaynoLikes.setText((Integer.toString(countLikes)+(" Likes")));

                 }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            })  ;
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage){
            profile_image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Glide.with(ctx).load(profileimage).into(profile_image);
        }
        public void setTime(String time){
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("  "+time);
        }
        public void setDate(String date){
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText(date);
        }
        public void setDescription(String description){
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }
        public void setPostimage(Context ctx, String postimage){
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Glide.with(ctx).load(postimage).into(PostImage);
        }
    }



    private void SendUsertoSetpActivity()
    {
        Intent intentsetup = new Intent(MainActivity.this,SetupActivity.class);
        intentsetup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentsetup);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem)
    {
        switch (menuItem.getItemId()){

            case R.id.nav_post:
                    startActivity(new Intent(getApplicationContext(),PostActivity.class));
                    break;
            case R.id.nav_profile:
                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                Intent friendIntent = new Intent(getApplicationContext(),FriendsActivity.class);
                startActivity(friendIntent);
                break;
            case R.id.nav_find_friends:
                startActivity(new Intent(getApplicationContext(),FindFriendsActivity.class));
                break;

            case R.id.nav_messages:
                Intent friendIntent1 = new Intent(getApplicationContext(),FriendsActivity.class);
                startActivity(friendIntent1);
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
//                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Logout:

                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }
    }
}
