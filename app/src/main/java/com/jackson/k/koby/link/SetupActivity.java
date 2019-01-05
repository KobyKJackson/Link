package com.jackson.k.koby.link;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{
    private EditText UserName, FullName, Country;
    private Button SaveButton;
    private CircleImageView ProfilePicture;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersReference;
    private StorageReference UserProfilePictureImageReference;

    private ProgressDialog loadingBar;

    String currentUserID;
    final static int galleryPic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfilePictureImageReference = FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        UserName = findViewById(R.id.setup_userName_EditText);
        FullName = findViewById(R.id.setup_fullName_EditText);
        Country = findViewById(R.id.setup_country_EditText);
        SaveButton = findViewById(R.id.setup_save_button);
        ProfilePicture = findViewById(R.id.setup_profile_picture_Image);
        loadingBar = new ProgressDialog(this);

        SaveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SaveAccountSetupInformation();
            }
        });

        ProfilePicture.setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Saving Profile Picture");
                loadingBar.setMessage("Please wait, saving your profile picture...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfilePictureImageReference.child(currentUserID+".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile picture stored successfully...", Toast.LENGTH_SHORT).show();
                            final String downloagURL = task.getResult().getDownloadUrl().toString();

                            //TODO: this should also be part of the user class
                            UsersReference.child("profilePicture").setValue(downloagURL)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profile picture updated successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            loadingBar.dismiss();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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

    private void SaveAccountSetupInformation()
    {
        String userName = UserName.getText().toString();
        String fullName = FullName.getText().toString();
        String country = Country.getText().toString();

        if(validateForm())
        {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait, updating your new Account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            //TODO: create a seperate class for this information and change the info to usefully things
            HashMap userMap = new HashMap();
            userMap.put("userName", userName);
            userMap.put("fullName", fullName);
            userMap.put("country", country);
            userMap.put("status", "Hey! Koby is awesome");
            userMap.put("gender", "none");
            userMap.put("DOB", "none");
            userMap.put("relationshipStatus", "none");

            UsersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Account has been created successfully.", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private boolean validateForm()
    {
        //TODO: Check all other usernames to see of it is already taken
        boolean result = true;
        if (TextUtils.isEmpty(UserName.getText().toString()))
        {
            UserName.setError("Required");
            result = false;
        }
        else
        {
            UserName.setError(null);
        }

        if (TextUtils.isEmpty(FullName.getText().toString()))
        {
            FullName.setError("Required");
            result = false;
        }
        else
        {
            FullName.setError(null);
        }

        if (TextUtils.isEmpty(Country.getText().toString()))
        {
            Country.setError("Required");
            result = false;
        }
        else
        {
            Country.setError(null);
        }
        return result;
    }
}
