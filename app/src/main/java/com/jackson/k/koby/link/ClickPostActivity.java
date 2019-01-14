package com.jackson.k.koby.link;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.DatabaseMetaData;

public class ClickPostActivity extends AppCompatActivity
{
    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostButton, EditPostButton;
    private String PostKey;
    private DatabaseReference ClickPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostImage = findViewById(R.id.clickPost_image_imageView);
        PostDescription = findViewById(R.id.clickPost_description_textView);
        DeletePostButton = findViewById(R.id.clickPost_delete_Button);
        EditPostButton = findViewById(R.id.clickPost_edit_Button);

        ClickPostRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String decription = dataSnapshot.child("description").getValue().toString();
                String image = dataSnapshot.child("postImage").getValue().toString();

                PostDescription.setText(decription);
                Picasso.with(ClickPostActivity.this).load(image).into(PostImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
