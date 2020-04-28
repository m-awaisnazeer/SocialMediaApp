package com.example.mysocialnetworkappcc;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mysocialnetworkappcc.Adapters.MessagesAdapter;
import com.example.mysocialnetworkappcc.Models.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private Toolbar chattoolbar;
    private ImageView SendImagefileIV, SendMessageIV;
    private EditText userMessageInput;
    private RecyclerView userMessageList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;


    private String messageReceiverID, messageReceiverName, messageSenderID;

    private TextView receierName;
    private CircleImageView receiverProfileImage;
    private DatabaseReference RootRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate , saveCurrentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
    RootRef = FirebaseDatabase.getInstance().getReference();

    messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();

        IntializeFields();
        DisplayReceiverInfo();
        SendMessageIV.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();
            }
        });

        FtechMessages();


    }

    private void FtechMessages()
    {
        RootRef.child("Messages").child(messageSenderID)
                .child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        if (dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage()
    {
        String messageText = userMessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Type something", Toast.LENGTH_SHORT).show();
        }else
            {
                String message_sender_ref = "Messages/"+ messageSenderID +"/"+ messageReceiverID;
                String message_receiver_ref = "Messages/"+ messageReceiverID +"/"+ messageSenderID;

                DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID)
                        .child(messageReceiverID).push(); //push will create random ID for US
                String message_push_id = user_message_key.getKey();



                Calendar calForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-YYYY");
                saveCurrentDate = currentDate.format(calForDate.getTime());

                Calendar calForTime = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                saveCurrentTime = currentTime.format(calForDate.getTime());

                Map messageTextBody = new HashMap();
                messageTextBody.put("message",messageText);
                messageTextBody.put("time",saveCurrentTime);
                messageTextBody.put("date",saveCurrentDate);
                messageTextBody.put("type","text");
                messageTextBody.put("from",messageSenderID);

                Map messageBodyDetails = new HashMap();
                messageBodyDetails.put(message_sender_ref+ "/" + message_push_id,  messageTextBody);
                messageBodyDetails.put(message_receiver_ref+ "/" + message_push_id,  messageTextBody);

                RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(
                        new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(ChatActivity.this, "Message send successfully", Toast.LENGTH_SHORT).show();
                                    userMessageInput.setText("");
                                }
                                else
                                {
                                    Toast.makeText(ChatActivity.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    userMessageInput.setText("");
                                }


                            }
                        }
                );



        }
    }

    private void DisplayReceiverInfo()
    {
        receierName.setText(messageReceiverName);

        RootRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                           {
                                               if (dataSnapshot.exists())
                                               {
                                                   final String profileImage =dataSnapshot.child("profileimage").getValue().toString();
                                                   Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                                               }

                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError databaseError) {

                                           }
                                       }
                );
    }

    private void IntializeFields()
    {
        chattoolbar = (Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(chattoolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(action_bar_view);


        receierName = (TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_imagee);

        SendImagefileIV = (ImageView)findViewById(R.id.send_image_file_IV);
        SendMessageIV = (ImageView)findViewById(R.id.send_message_IV);
        userMessageInput = (EditText)findViewById(R.id.input_message);


        messagesAdapter = new MessagesAdapter(messagesList);
        userMessageList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);



    }
}
