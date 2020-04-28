package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName , FullName , CountryName;
    private Button SaveInformationButtion;
    private CircleImageView profileimage;

    private FirebaseAuth mAuth;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageRef;
    private String currentUserId;
    final static int Gallery_Pick = 1;

    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        UserName = (EditText)findViewById(R.id.setup_username);
        FullName = (EditText)findViewById(R.id.setup_full_name);
        CountryName = (EditText)findViewById(R.id.setup_country_name);

        SaveInformationButtion = (Button)findViewById(R.id.setup_information_button);
        profileimage = (CircleImageView)findViewById(R.id.setup_profile_image);

        pd = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");



        SaveInformationButtion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInformation();
            }
        });
        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_Pick);

            }
        });
        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Glide.with(getApplicationContext()).load(image).into(profileimage);
                    }else {
                        Toast.makeText(SetupActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_Pick && resultCode ==RESULT_OK &&  data!= null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode ==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                pd.setTitle("Profile Image");
                pd.setMessage("Updating Profile Picture");
                pd.show();
                pd.setCanceledOnTouchOutside(true);
                Uri resultUri = result.getUri();
                StorageReference filepath = UserProfileImageRef.child(currentUserId+".jpg");
                UploadTask uploadTask = filepath.putFile(resultUri);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                 @Override
                                 public void onSuccess(Uri uri) {
                                     String download_url = uri.toString();
                                     UserRef.child("profileimage").setValue(download_url);
                                     Toast.makeText(SetupActivity.this, "Image Store to database successfully", Toast.LENGTH_SHORT).show();
                                     pd.dismiss();
                                 }
                             }).addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {
                                     Toast.makeText(SetupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                     pd.dismiss();
                                 }
                             });
                    }
                });

            }else {
                Toast.makeText(this, "Image dont cropped, Try Again", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }
        }

    }

    private void SaveAccountSetupInformation()
    {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();
        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "Enter Username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Enter fullname", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "Enter country", Toast.LENGTH_SHORT).show();
        }else {
            pd.setTitle("Account Information");
            pd.setMessage("Saving account Information");
            pd.show();
            pd.setCanceledOnTouchOutside(true);
            HashMap usermap = new HashMap();
            usermap.put("username" , username);
            usermap.put("fullname",fullname);
            usermap.put("country",country);
            usermap.put("status" , "My Status,");
            usermap.put("gender","none");
            usermap.put("dob","none");
            usermap.put("relationshipstatus","none");
            UserRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {

                    Toast.makeText(SetupActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    finish();
                    pd.dismiss();
                }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SetupActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });

        }
    }
}
