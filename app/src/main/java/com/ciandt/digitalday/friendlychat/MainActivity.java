package com.ciandt.digitalday.friendlychat;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String MESSAGES_CHILD = "messages";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_SELECTOR = 999;

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
    private GoogleApiClient mGoogleApiClient;
    private FirebaseStorage firebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseStorage = FirebaseStorage.getInstance();

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

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

        adapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(data.getData(), filePathColumn, null, null, null);
            if (cursor == null || cursor.getCount() < 1) {
                return;
            }
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            if(columnIndex < 0) { // no column index
                return;
            }
            File currentPhoto = new File(cursor.getString(columnIndex));
            cursor.close();

            uploadPhoto(currentPhoto);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadPhoto(File photo) {

        progressBar.show();

        Uri file = Uri.fromFile(photo);

        // Create a firebaseStorage reference from our app
        StorageReference storageRef = firebaseStorage.getReferenceFromUrl("gs://digitalday-aba51.appspot.com");

        // Create a reference to "photo.jpg"
        StorageReference photoRef = storageRef.child(file.getLastPathSegment());

        photoRef.putFile(file).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.hide();
                Log.d(TAG, e.getMessage(), e);
            }
        }).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri uri = taskSnapshot.getDownloadUrl();
                sendMessageWithPhoto(uri);
            }
        });
    }

    private void sendMessageWithPhoto(Uri photo) {
        Message message = new Message();
        message.setName(username);
        message.setAvatar(photoUrl);
        message.setPhoto(photo.toString());
        message.setTypeMessage(2);
        messageReference.push().setValue(message);
    }

    private void signOut() {
        firebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        firebaseUser = null;
        username = "ANONYMOUS";
        photoUrl = null;
        startActivity(new Intent(this, SignInActivity.class));
        finish();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        adapter = new MessageAdapter(this, new ArrayList<Message>());
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.messageRecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        progressBar = (ContentLoadingProgressBar) findViewById(R.id.progressBar);

        ImageButton btnGallery = (ImageButton)findViewById(R.id.open_gallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchPhotoSelectionIntent();
            }
        });

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
                message.setId(dataSnapshot.getKey());
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void dispatchPhotoSelectionIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        this.startActivityForResult(galleryIntent, REQUEST_IMAGE_SELECTOR);
    }
}
