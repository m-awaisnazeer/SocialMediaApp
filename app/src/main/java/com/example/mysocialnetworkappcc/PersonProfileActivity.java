package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {
    private CircleImageView profileactivity_image;
    private TextView profileactivity_name,profileactivity_username, profileactivity_description , profileactivity_dob , profileactivity_gender ,profileactivity_country ,profileactivity_relationship;
    private TextView sendFriendRequest, CancelFriendRequest;

    private DatabaseReference FriendRequestRef , UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String SenderUserID , ReceiveruserID, CURRENT_STATE;
    private String saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        ReceiveruserID = getIntent().getExtras().get("visit_user_id").toString();

        mAuth  = FirebaseAuth.getInstance();
        SenderUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(ReceiveruserID);
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        InitializeFields();


        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String mProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String mUserName = dataSnapshot.child("username").getValue().toString();
                    String mProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String mProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String mDOB = dataSnapshot.child("dob").getValue().toString();
                    String mCountry = dataSnapshot.child("country").getValue().toString();
                    String mGender = dataSnapshot.child("gender").getValue().toString();
                    String mRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(mProfileImage).into(profileactivity_image);
                    profileactivity_country.setText("Country:"+mCountry);
                    profileactivity_description.setText(mProfileStatus);
                    profileactivity_dob.setText("DOB:"+mDOB);
                    profileactivity_gender.setText("Gender:"+mGender);
                    profileactivity_name.setText(mProfileName);
                    profileactivity_username.setText("@"+mUserName);
                    profileactivity_relationship.setText("Relation:"+mRelationStatus);

                    MaintenanceofButtons();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        CancelFriendRequest.setVisibility(View.INVISIBLE);
        CancelFriendRequest.setEnabled(false);


        if (!SenderUserID.equals(ReceiveruserID))
        {
            sendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequest.setEnabled(false);

                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent"))
                    {
                        CancelFriendRequestMethod();
                    }
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnfriendExistingFriend();
                    }
                }
            });
        }
        else
        {
            CancelFriendRequest.setVisibility(View.INVISIBLE);
            sendFriendRequest.setVisibility(View.INVISIBLE);
        }

    }

    private void UnfriendExistingFriend()
    {
        FriendsRef.child(SenderUserID).child(ReceiveruserID)
                .child("request_type").removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(ReceiveruserID).child(SenderUserID)
                                    .child("request_type").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendFriendRequest.setEnabled(true);
                                                CURRENT_STATE ="not_friends";
                                                sendFriendRequest.setText("Send Friend Request");

                                                CancelFriendRequest.setVisibility(View.INVISIBLE);
                                                CancelFriendRequest.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }


    private void AcceptFriendRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        FriendsRef.child(SenderUserID).child(ReceiveruserID)
                .child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(ReceiveruserID).child(SenderUserID)
                                    .child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                FriendRequestRef.child(SenderUserID).child(ReceiveruserID)
                                                        .child("request_type").removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(ReceiveruserID).child(SenderUserID)
                                                                            .child("request_type").removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if (task.isSuccessful())
                                                                                    {
                                                                                        sendFriendRequest.setEnabled(true);
                                                                                        CURRENT_STATE ="friends";
                                                                                        sendFriendRequest.setText("Unfriend this Person");

                                                                                        CancelFriendRequest.setVisibility(View.INVISIBLE);
                                                                                        CancelFriendRequest.setEnabled(false);
                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void CancelFriendRequestMethod()
    {
        FriendRequestRef.child(SenderUserID).child(ReceiveruserID)
                .child("request_type").removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(ReceiveruserID).child(SenderUserID)
                                    .child("request_type").removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendFriendRequest.setEnabled(true);
                                                CURRENT_STATE ="not_friends";
                                                sendFriendRequest.setText("Send Friend Request");

                                                CancelFriendRequest.setVisibility(View.INVISIBLE);
                                                CancelFriendRequest.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void MaintenanceofButtons()
    {
        FriendRequestRef.child(SenderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild(ReceiveruserID))
                {
                    String request_type = dataSnapshot.child(ReceiveruserID).child("request_type").getValue().toString();

                    if (request_type.equals("sent"))
                    {
                        CURRENT_STATE = "request_sent";
                        sendFriendRequest.setText("Cancel Friend Request");

                        CancelFriendRequest.setVisibility(View.INVISIBLE);
                        CancelFriendRequest.setEnabled(false);
                    }
                    else if (request_type.equals("received"))
                    {
                        CURRENT_STATE = "request_received";
                        sendFriendRequest.setText("Accept friend Request");

                        CancelFriendRequest.setVisibility(View.VISIBLE);
                        CancelFriendRequest.setEnabled(true);

                        CancelFriendRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequestMethod();
                            }
                        });
                    }
                }
                else
                {
                    FriendsRef.child(SenderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(ReceiveruserID))
                                    {
                                        CURRENT_STATE = "friends";
                                        sendFriendRequest.setText("Unfriend this person");

                                        CancelFriendRequest.setVisibility(View.INVISIBLE);
                                        CancelFriendRequest.setEnabled(false);

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendFriendRequestToaPerson()
    {
        FriendRequestRef.child(SenderUserID).child(ReceiveruserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(ReceiveruserID).child(SenderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendFriendRequest.setEnabled(true);
                                                CURRENT_STATE ="request_sent";
                                                sendFriendRequest.setText("Cancel Friend Request");

                                                CancelFriendRequest.setVisibility(View.INVISIBLE);
                                                CancelFriendRequest.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void InitializeFields()
    {
        profileactivity_image = findViewById(R.id.person_profile_pic);
        profileactivity_name = (TextView)findViewById(R.id.person_full_name);
        profileactivity_username = (TextView)findViewById(R.id.person_username);
        profileactivity_description = (TextView)findViewById(R.id.person_profile_status);
        profileactivity_dob = (TextView)findViewById(R.id.person_dob);
        profileactivity_gender = (TextView)findViewById(R.id.person_gender);
        profileactivity_country = (TextView)findViewById(R.id.person_country);
        profileactivity_relationship = (TextView)findViewById(R.id.person_relationship);
        sendFriendRequest = (TextView) findViewById(R.id.send_request);
        CancelFriendRequest = (TextView)findViewById(R.id.cancel_friend_request);


        CURRENT_STATE = "not_friends";

    }
}
