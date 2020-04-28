package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView profileimg;
    Toolbar mToolbar;
    private EditText profile_status,setting_username,setting_profile_name,setting_country,setting_dob,setting_gender,setting_relation;
    Button update_settings_btn;
    private DatabaseReference SettingsuserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingsuserRef = FirebaseDatabase.getInstance().getReference().child("User").child(currentUserID);


        profileimg = findViewById(R.id.settings_profile_image);
        mToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        profile_status = findViewById(R.id.profile_status);
        setting_username = findViewById(R.id.setting_username);
        setting_profile_name = findViewById(R.id.setting_profile_name);
        setting_country = findViewById(R.id.setting_country);
        setting_dob = findViewById(R.id.setting_dob);
        setting_gender = findViewById(R.id.setting_gender);
        setting_relation = findViewById(R.id.setting_relation);
        update_settings_btn =findViewById(R.id.update_settings_btn);
        update_settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAccountInfo();
                Toast.makeText(SettingsActivity.this, "Tested", Toast.LENGTH_SHORT).show();

            }
        });

        SettingsuserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myPrfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelatioStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).into(profileimg);

                    profile_status.setText("myStatus");
                    setting_username.setText("myUserName");
                    setting_profile_name.setText(myPrfileName);
                    setting_country.setText(myCountry);
                    setting_dob.setText(myDob);
                    setting_gender.setText(myGender);
                    setting_relation.setText(myRelatioStatus);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ValidateAccountInfo()
    {
        String username = setting_username.getText().toString();

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

        return super.onOptionsItemSelected(item);

    }
}
