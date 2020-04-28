package com.example.mysocialnetworkappcc;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private  TextView UpdatePostTV;
    private ImageView SelectPostImage;
    private EditText PostDescription;
    private ProgressDialog pd;

    private static final int Gallery_Pick =1;
    private Uri ImageUri;
    private  String Description;
    private String saveCurrentDate , saveCurrentTime, postRandomName,DownloadURL,current_user_id;
    private long countPosts =0;

    private StorageReference PostsImagesReferences;
    private DatabaseReference usersRef , PostsRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);


        PostsImagesReferences = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.update_post);

        SelectPostImage = (ImageView) findViewById(R.id.select_post_image);
        PostDescription = (EditText) findViewById(R.id.post_description);
        UpdatePostTV = (TextView)findViewById(R.id.update_post_textView);
        pd = new ProgressDialog(this);

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
        UpdatePostTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) 
            {
                ValidatePostInfo();

            }
        });
    }

    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();
//        if (ImageUri == null){
//            Toast.makeText(this, "Select post Image", Toast.LENGTH_SHORT).show();
//        }
//        if (TextUtils.isEmpty(Description)){
//            Toast.makeText(this, "Say Something about your Post", Toast.LENGTH_SHORT).show();
//        }else {
//            pd.setTitle("Add New Post");
//            pd.setMessage("Updating new Post");
//            pd.show();
//            pd.setCanceledOnTouchOutside(true);
//            StoringImageToFirebaseStorage();
//        }
        pd.setTitle("Add New Post");
        pd.setMessage("Updating new Post");
        pd.show();
        pd.setCanceledOnTouchOutside(true);
        StoringImageToFirebaseStorage();
    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate+saveCurrentTime;

        StorageReference filePath = PostsImagesReferences.child("Post Images").child(ImageUri.getLastPathSegment()+postRandomName+".jpg");
        UploadTask uploadTask = filePath.putFile(ImageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DownloadURL = uri.toString();
                        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        SavingPostInformationToDatabase();
                        //pd.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                        pd.dismiss();
                    }
                });
            }
        });






//        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//           if (task.isSuccessful()){
//               Toast.makeText(PostActivity.this, "Image uploaded to Storage", Toast.LENGTH_SHORT).show();
//           }else {
//               Toast.makeText(PostActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//           }
//            }
//        });

    }

    private void SavingPostInformationToDatabase()
    {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                   countPosts = dataSnapshot.getChildrenCount();
                }
                else {
                 countPosts = 0;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    final String userfullName = dataSnapshot.child("fullname").getValue().toString();
                    final String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postMap = new HashMap();
                    postMap.put("uid",current_user_id);
                    postMap.put("date",saveCurrentDate);
                    postMap.put("time",saveCurrentTime);
                    postMap.put("description",Description);
                    postMap.put("postimage",DownloadURL);
                    postMap.put("profileimage",userProfileImage);
                    postMap.put("fullname",userfullName);
                    postMap.put("counter",countPosts);
                    PostsRef.child(current_user_id+postRandomName).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(PostActivity.this, "New Post is updated Successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                        pd.dismiss();;
                                    }else {
                                        Toast.makeText(PostActivity.this, "Error:\n"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        pd.dismiss();
                                    }
                                }
                            });



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode ==Gallery_Pick && resultCode ==RESULT_OK && data != null){
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }

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

