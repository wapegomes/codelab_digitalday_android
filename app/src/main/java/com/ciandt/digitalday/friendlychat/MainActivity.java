package com.ciandt.digitalday.friendlychat;

import android.content.Intent;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGES_CHILD = "messages";

    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private String username = "ANONYMOUS";
    private String photoUrl;
    private DatabaseReference messageReference;
    private MessageAdapter adapter;
    private Button sendButton;
    private EditText messageEditText;
    private ContentLoadingProgressBar progressBar;
    private ChildEventListener childEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        }
        else {
            username = firebaseUser.getDisplayName();
            photoUrl = firebaseUser.getPhotoUrl().toString();
        }

        messageReference = FirebaseDatabase.getInstance()
                .getReference().child(MESSAGES_CHILD);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        configEventMessage();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (childEventListener != null) {
            messageReference.removeEventListener(childEventListener);
        }
    }

    private void initView() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        adapter = new MessageAdapter(this, new ArrayList<Message>());
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.messageRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progressBar);

        configSendButton();

        configMessageEditText();
    }

    private void configMessageEditText() {
        messageEditText = (EditText)findViewById(R.id.messageEditText);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void configSendButton() {
        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message(messageEditText.getText().toString(), username, photoUrl);
                messageReference.push().setValue(message);
                messageEditText.setText("");
            }
        });
    }

    private void configEventMessage() {
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                progressBar.hide();
                Message message = dataSnapshot.getValue(Message.class);
                adapter.add(message);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        messageReference.addChildEventListener(childEventListener);
    }

}
