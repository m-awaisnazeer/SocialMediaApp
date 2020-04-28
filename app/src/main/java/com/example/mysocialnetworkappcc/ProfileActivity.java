package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    ImageView profileactivity_backarrow;
    private CircleImageView profileactivity_image;
    private TextView profileactivity_name,profileactivity_username, profileactivity_description , profileactivity_dob , profileactivity_gender ,profileactivity_country ,profileactivity_relationship;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth  = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        profileactivity_image = findViewById(R.id.profileactivity_image);
        profileactivity_backarrow = findViewById(R.id.profileactivity_backarrow);
        profileactivity_name = (TextView)findViewById(R.id.profileactivity_name);
        profileactivity_username = (TextView)findViewById(R.id.profileactivity_username);
        profileactivity_description = (TextView)findViewById(R.id.profileactivity_description);
        profileactivity_dob = (TextView)findViewById(R.id.profileactivity_dob);
        profileactivity_gender = (TextView)findViewById(R.id.profileactivity_gender);
        profileactivity_country = (TextView)findViewById(R.id.profileactivity_country);
        profileactivity_relationship = (TextView)findViewById(R.id.profileactivity_relationship);

        profileactivity_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });


        profileUserRef.addValueEventListener(new ValueEventListener() {
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

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
