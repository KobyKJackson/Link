package com.jackson.k.koby.link;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private TextView profileStatus, userName, fullName, country, dateOfBirth, relationshipStatus, gender;
    private CircleImageView profilePicture;

    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileStatus = findViewById(R.id.profile_status);
        userName = findViewById(R.id.profile_userName);
        fullName = findViewById(R.id.profile_fullName);
        country = findViewById(R.id.profile_country);
        dateOfBirth = findViewById(R.id.profile_dateOfBirth);
        relationshipStatus = findViewById(R.id.profile_relationshipStatus);
        gender = findViewById(R.id.profile_gender);
        profilePicture = findViewById(R.id.profile_profilePicture);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        SettingsUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

        SettingsUserRef.addValueEventListener(new ValueEventListener() //TODO: when user clicks outside keyboard it closes.
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()) {
                    String myProfileImage = dataSnapshot.child("profilePicture").getValue().toString();
                    String myUserName = dataSnapshot.child("userName").getValue().toString();
                    String myFullName = dataSnapshot.child("fullName").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myDateOfBirth = dataSnapshot.child("DOB").getValue().toString();
                    String myRelationshipStatus = dataSnapshot.child("relationshipStatus").getValue().toString();

                    Picasso.with(ProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(profilePicture);

                    userName.setText("@" + myUserName);
                    fullName.setText(myFullName);
                    profileStatus.setText(myProfileStatus);
                    country.setText("Country: " + myCountry);
                    gender.setText("Gender: " + myGender);
                    dateOfBirth.setText("Birthday: " + myDateOfBirth);
                    relationshipStatus.setText("Relationship Status: " + myRelationshipStatus);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
