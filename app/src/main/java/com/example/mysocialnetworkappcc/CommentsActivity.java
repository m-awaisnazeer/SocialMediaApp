package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysocialnetworkappcc.Models.Comments;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton postCommentButton;
    private EditText CommentInputtext;
    private RecyclerView CommentsList;

    private DatabaseReference UsersRef,PostsRef;
    private FirebaseAuth mAuth;

    private  String Post_Key,currentuserID;
    private FirebaseRecyclerAdapter commentsadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);


        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentuserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");



        CommentsList = (RecyclerView) findViewById(R.id.comments_lists);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputtext = (EditText) findViewById(R.id.comment_input);
        postCommentButton = (ImageButton) findViewById(R.id.post_comment_btn);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UsersRef.child(currentuserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                     if (dataSnapshot.exists())
                     {
                         String userName = dataSnapshot.child("username").getValue().toString();

                         validateComment(userName);

                         CommentInputtext.setText("");
                     }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Query query = PostsRef;
        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>().
                setQuery(query, new SnapshotParser<Comments>() {
                    @NonNull
                    @Override
                    public Comments parseSnapshot(@NonNull DataSnapshot snapshot) {
                        return new Comments(snapshot.child("comment").getValue().toString(),
                                snapshot.child("date").getValue().toString(),
                                snapshot.child("time").getValue().toString(),
                                snapshot.child("username").getValue().toString());
                    }
                })
                .build();

        commentsadapter = new FirebaseRecyclerAdapter<Comments,CommentsViewHolder>(options)
        {

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_comments_layout, parent, false);

                return new CommentsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                holder.setUsername(model.getUsername());
                holder.setComment(model.getComment());
                holder.setDate(model.getDate());
                holder.setTime(model.getTime());

            }
        };
        CommentsList.setAdapter(commentsadapter);
        commentsadapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setUsername(String username)
        {
            TextView myUsername = mView.findViewById(R.id.comment_username);
            myUsername.setText("@"+username+"  ");
        }
        public void setTime(String time)
        {
            TextView myTime = mView.findViewById(R.id.comment_time);
            myTime.setText(" "+time);
        }
        public void setDate(String date)
        {

            TextView mydate = mView.findViewById(R.id.comment_date);
            mydate.setText(" "+date);
        }
        public void setComment(String comment)
        {
            TextView myComment = mView.findViewById(R.id.comment_text);
            myComment.setText(comment);

        }

    }







    private void validateComment(String userName)
    {
        String commentText = CommentInputtext.getText().toString();

        if (TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Write Sommething", Toast.LENGTH_SHORT).show();
        }
        else

        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForDate.getTime());

            final String RandomKey = currentuserID+ saveCurrentDate+saveCurrentTime;

            HashMap commentsmap = new HashMap();
            commentsmap.put("uid",currentuserID);
            commentsmap.put("comment",commentText);
            commentsmap.put("date",saveCurrentDate);
            commentsmap.put("time",saveCurrentTime);
            commentsmap.put("username",userName);

            PostsRef.child(RandomKey).updateChildren(commentsmap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(CommentsActivity.this, "Comment Successfully", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {

                    Toast.makeText(CommentsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }
}
