package com.jackson.k.koby.link;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

//TODO: make posts have more options such as no picture
public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    private Button AddNewPostButton;
    private ImageButton SelectPostImage;
    private EditText PostDescription;
    Uri ImageUri;
    final static int galleryPic = 1;
    private StorageReference PostsImagesReference;
    private ProgressDialog loadingBar;
    private String DownloadURL;
    private DatabaseReference UserRef, PostRef;
    private FirebaseAuth mAuth;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        PostRef = FirebaseDatabase.getInstance().getReference().child("Posts"); //TODO: Check this

        AddNewPostButton = findViewById(R.id.addNewPost_Button);
        SelectPostImage = findViewById(R.id.addNewPost_Image);
        PostDescription = findViewById(R.id.addNewPostDescription_EditText);


        loadingBar = new ProgressDialog(this);

        mToolbar = findViewById(R.id.newPost_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) 
            {
                OpenGallery();
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                UpdateNewPost();
            }
        });
    }

    private void UpdateNewPost()
    {
        if(validateForm())
        {
            StoreImageToDatabase();
        }
    }

    private void StoreImageToDatabase()
    {
        String timeStamp = new SimpleDateFormat("_dd-MM-yyyy_HHmmss").format(new Date());

        StorageReference filePath = PostsImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + timeStamp + ".jpg"); //TODO:Maybe re-structure the pictures base off users or add more of a random value so two aren't the same

        loadingBar.setTitle("Posting...");
        loadingBar.setMessage("Please wait, while we are save image to the database...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DownloadURL = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image uploaded to Database", Toast.LENGTH_SHORT).show();

                    SavePostInformationToDatabase();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SavePostInformationToDatabase()
    {
        UserRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String fullName = dataSnapshot.child("fullName").getValue().toString();
                    String profilePicture = dataSnapshot.child("profilePicture").getValue().toString();

                    Calendar calFordDate = Calendar.getInstance();
                    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                    String saveCurrentDate = currentDate.format(calFordDate.getTime());

                    Calendar calFordTime = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
                    String saveCurrentTime = currentTime.format(calFordDate.getTime());

                    HashMap postMap = new HashMap(); //TODO: create a class for this;
                    postMap.put("uid", currentUserID);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", PostDescription.getText().toString());
                    postMap.put("postImage", DownloadURL);
                    postMap.put("profilePicture", profilePicture);
                    postMap.put("fullName", fullName);
                    PostRef.child(currentUserID+saveCurrentDate+saveCurrentTime).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Post uploaded to Database", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(PostActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private boolean validateForm()
    {
        boolean result = true;

        if (TextUtils.isEmpty(PostDescription.getText().toString()))
        {
            PostDescription.setError("Required");
            result = false;
        }
        else
        {
            PostDescription.setError(null);
        }

        if (ImageUri == null)
        {
            Toast.makeText(PostActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
            result = false;
        }
        return result;
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(data.getData());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            SendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);//, ActivityOptions.makeSceneTransitionAnimation(this).toBundle()); //TODO: Make transitions between activities smoother with animations.
        finish();
    }
}
