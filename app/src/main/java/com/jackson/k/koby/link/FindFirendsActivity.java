package com.jackson.k.koby.link;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFirendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultsList;

    private DatabaseReference allUseresDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_firends);

        allUseresDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = findViewById(R.id.findFriends_AppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchResultsList = findViewById(R.id.findFriends_searchResults);
        searchResultsList.setHasFixedSize(true);
        searchResultsList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = findViewById(R.id.findFriends_searchButton);
        searchInputText = findViewById(R.id.findFriends_search);

        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SearchForUsers();
            }
        });

    }

    private void SearchForUsers()
    {
        String searchInput = searchInputText.getText().toString();

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

        Query searchUsersQuery = allUseresDatabaseRef.orderByChild("fullName").startAt(searchInput).endAt(searchInput + "\uf8ff"); //TODO: Uppercase/lowercase search must be correct need to fix to allow all

        FirebaseRecyclerAdapter<FindFriends, FindFirendsActivity.FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>
                (
                        FindFriends.class,
                        R.layout.all_users_display_layout,
                        FindFriendsViewHolder.class,
                        searchUsersQuery
                )
        {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder viewHolder, FindFriends model, int position)
            {
                 viewHolder.setFullName(model.getFullName());
                 viewHolder.setProfilePicture(getApplicationContext(), model.getProfilePicture());
                 viewHolder.setStatus(model.getStatus());
             }
         };
         searchResultsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FindFriendsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setFullName(String fullName)
        {
            TextView lFullName = mView.findViewById(R.id.all_users_fullName);
            lFullName.setText(fullName);
        }

        public void setProfilePicture(Context ctx, String profilePicture)
        {
            CircleImageView profilePic = mView.findViewById(R.id.all_users_profile_picture);
            Picasso.with(ctx).load(profilePicture).into(profilePic);
        }

        public void setStatus(String status)
        {
            TextView lStatus = mView.findViewById(R.id.all_users_status);
            lStatus.setText(status);
        }
    }
}
