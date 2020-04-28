package com.example.mysocialnetworkappcc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private EditText UserEmail, UserPassword, UserConfirmPassword;
    private Button CreateAccountButton;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        UserEmail = (EditText)findViewById(R.id.register_email);
        UserPassword = (EditText)findViewById(R.id.register_password);
        UserConfirmPassword = (EditText)findViewById(R.id.register_confirm_password);
        CreateAccountButton = (Button)findViewById(R.id.register_create_account);
        pd = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
      //  uid = mAuth.getCurrentUser().getUid();


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CreateNewAccount();

            }
        });

    }

    private void CreateNewAccount()
    {
     String email = UserEmail.getText().toString();
     String passwprd = UserPassword.getText().toString();
     String confirmPassword = UserConfirmPassword.getText().toString();

     if (TextUtils.isEmpty(email))
     {
         Toast.makeText(this, "Enter your Email..", Toast.LENGTH_SHORT).show();
     }
     else if (TextUtils.isEmpty(passwprd))
     {
         Toast.makeText(this, "Enter your Password..", Toast.LENGTH_SHORT).show();
     }
     else if (TextUtils.isEmpty(confirmPassword))
     {
         Toast.makeText(this, "Confirm your  Password..", Toast.LENGTH_SHORT).show();
     }
     else if (!passwprd.equals(confirmPassword))
     {
         Toast.makeText(this, "Password do not match with confirm password", Toast.LENGTH_SHORT).show();
     }
     else
     {
         pd.setTitle("Creating New Account");
         pd.setMessage("Creating new Account");
         pd.show();
         pd.setCanceledOnTouchOutside(true);

         mAuth.createUserWithEmailAndPassword(email,passwprd)
                 .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) 
                     {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(RegisterActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                            pd.dismiss();
                            Intent intent = new Intent(RegisterActivity.this,SetupActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        }
                     }
                 }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                 pd.dismiss();
             }
         });
     }
    }
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null)
        {
            SendUserToMainActivity();
        }
    }
    private void SendUserToMainActivity()
    {
        Intent intentsetup = new Intent(RegisterActivity.this,MainActivity.class);
        intentsetup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentsetup);
        finish();
    }
}
