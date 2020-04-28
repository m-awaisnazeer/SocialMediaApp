package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private ImageView PostImage;
    private TextView PostDescription;
    FloatingActionButton edit_post_fab , delete_post_fab;
    FloatingActionsMenu floatingActionsMenu;



    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currentUserID, databaseUserID ,description,image;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        setContentView(R.layout.activity_click_post);
        PostImage = findViewById(R.id.click_post_image);
        PostDescription = findViewById(R.id.click_post_description);
        edit_post_fab = findViewById(R.id.edit_post_fab);
        delete_post_fab = findViewById(R.id.delete_post_fab);
        floatingActionsMenu = findViewById(R.id.floating_action_menu);
        floatingActionsMenu.setVisibility(View.INVISIBLE);

        mToolbar = (Toolbar) findViewById(R.id.edit_post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_post);




        edit_post_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCurrentPost(description);
                showToast("Edit Post");
            }
        });
        delete_post_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
               if (dataSnapshot.exists())
               {
                   description = dataSnapshot.child("description").getValue().toString();
                   image = dataSnapshot.child("postimage").getValue().toString();
                   databaseUserID = dataSnapshot.child("uid").getValue().toString();
                   PostDescription.setText(description);
                   Picasso.get().load(image).into(PostImage);

                   if (currentUserID.equals(databaseUserID))
                   {

                       floatingActionsMenu.setVisibility(View.VISIBLE);
                   }
               }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void EditCurrentPost(String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post Update Successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
       // dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_blue_dark);

    }

    private void DeleteCurrentPost()
    {
        clickPostRef.removeValue();
        SendUserToMainActivity();
        showToast("Post has been deleted");
        //Picture from storage not deleted. delete it in future

    }

    private void SendUserToMainActivity()
    {
        Intent intent = new Intent(ClickPostActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    public void showToast(String stg){
        Toast.makeText(this, stg, Toast.LENGTH_SHORT).show();
    }
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }

        return super.onOptionsItemSelected(item);

    }
}
