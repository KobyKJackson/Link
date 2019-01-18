package com.jackson.k.koby.link;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
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

public class SettingsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private EditText profileStatus, userName, fullName, country, dateOfBirth, relationshipStatus, gender;
    private CircleImageView profilePicture;
    private Button updateProfileButton;

    private StorageReference UserProfilePictureImageReference;
    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    private ProgressDialog loadingBar;

    final static int galleryPic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfilePictureImageReference = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileStatus = findViewById(R.id.settings_status);
        userName = findViewById(R.id.settings_userName);
        fullName = findViewById(R.id.settings_fullName);
        country = findViewById(R.id.settings_country);
        dateOfBirth = findViewById(R.id.settings_dateOfBirth);
        relationshipStatus = findViewById(R.id.settings_relationshipStatus);
        gender = findViewById(R.id.settings_gender);
        profilePicture = findViewById(R.id.settings_profileImage);
        updateProfileButton = findViewById(R.id.settings_updateSettingsButton);

        loadingBar = new ProgressDialog(this);

        SettingsUserRef.addValueEventListener(new ValueEventListener() //TODO: when user clicks outside keyboard it closes.
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String myProfileImage = dataSnapshot.child("profilePicture").getValue().toString();
                String myUserName = dataSnapshot.child("userName").getValue().toString();
                String myFullName = dataSnapshot.child("fullName").getValue().toString();
                String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                String myCountry = dataSnapshot.child("country").getValue().toString();
                String myGender = dataSnapshot.child("gender").getValue().toString();
                String myDateOfBirth = dataSnapshot.child("DOB").getValue().toString();
                String myRelationshipStatus = dataSnapshot.child("relationshipStatus").getValue().toString();

                Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(profilePicture);
                userName.setText(myUserName);
                fullName.setText(myFullName);
                profileStatus.setText(myProfileStatus);
                country.setText(myCountry);
                gender.setText(myGender);
                dateOfBirth.setText(myDateOfBirth);
                relationshipStatus.setText(myRelationshipStatus);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(validateForm())
                {
                    updateSettingsInDatabase();
                }
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });
    }

    private boolean validateForm()
    {
        //TODO: Check all other usernames to see of it is already taken
        boolean result = true;
        if (TextUtils.isEmpty(profileStatus.getText().toString()))
        {
            profileStatus.setError("Required");
            result = false;
        }
        else
        {
            profileStatus.setError(null);
        }

        if (TextUtils.isEmpty(userName.getText().toString()))
        {
            userName.setError("Required");
            result = false;
        }
        else
        {
            userName.setError(null);
        }

        if (TextUtils.isEmpty(fullName.getText().toString()))
        {
            fullName.setError("Required");
            result = false;
        }
        else
        {
            fullName.setError(null);
        }

        if (TextUtils.isEmpty(country.getText().toString()))
        {
            country.setError("Required");
            result = false;
        }
        else
        {
            country.setError(null);
        }

        if (TextUtils.isEmpty(dateOfBirth.getText().toString()))
        {
            dateOfBirth.setError("Required");
            result = false;
        }
        else
        {
            dateOfBirth.setError(null);
        }
        if (TextUtils.isEmpty(relationshipStatus.getText().toString()))
        {
            relationshipStatus.setError("Required");
            result = false;
        }
        else
        {
            relationshipStatus.setError(null);
        }
        if (TextUtils.isEmpty(gender.getText().toString()))
        {
            gender.setError("Required");
            result = false;
        }
        else
        {
            gender.setError(null);
        }

        return result;
    }

    private void updateSettingsInDatabase()
    {
        String lProfileStatus = profileStatus.getText().toString();
        String lUserName = userName.getText().toString();
        String lFullName = fullName.getText().toString();
        String lCountry = country.getText().toString();
        String lDateOfBirth = dateOfBirth.getText().toString();
        String lRelationshipStatus = relationshipStatus.getText().toString();
        String lGender = gender.getText().toString();

        HashMap userMap = new HashMap();
            userMap.put("status", lProfileStatus);
            userMap.put("userName", lUserName);
            userMap.put("fullName", lFullName);
            userMap.put("country", lCountry);
            userMap.put("DOB", lDateOfBirth);
            userMap.put("relationshipStatus", lRelationshipStatus);
            userMap.put("gender", lGender);

        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener()
        {
            public void onComplete(@NonNull Task task)
            {
                if (task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Updating profile settings...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);//, ActivityOptions.makeSceneTransitionAnimation(this).toBundle()); //TODO: Make transitions between activities smoother with animations.
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Saving Profile Picture");
                loadingBar.setMessage("Please wait, saving your profile picture...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfilePictureImageReference.child(currentUserID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile picture stored successfully...", Toast.LENGTH_SHORT).show();
                            final String downloagURL = task.getResult().getDownloadUrl().toString();

                            //TODO: this should also be part of the user class
                            SettingsUserRef.child("profilePicture").setValue(downloagURL)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "Profile picture updated successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            loadingBar.dismiss();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error: Image could not be cropped. Try again." , Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }
}
