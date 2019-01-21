package com.jackson.k.koby.link;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.spec.PSSParameterSpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mainToolbar;
    private ImageButton AddNewPostButton;

    private FirebaseAnalytics mFirebaseAnalytics;

    private CircleImageView NavProfilePicture;
    private TextView NavFullName;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersReference, PostsReference, LikesReference;

    String currentUserID;

    Boolean likeCheck = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesReference = FirebaseDatabase.getInstance().getReference().child("Likes");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setUserId(currentUserID);

        mainToolbar = findViewById(R.id.main_page_toolbar);
        drawerLayout = findViewById(R.id.drawable_layout);

        //Setting up the toggle button in the app bar
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Home");
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_closed);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AddNewPostButton = findViewById(R.id.addNewPost_Button);

        postList = findViewById(R.id.allUsersPosts_List);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfilePicture = navView.findViewById(R.id.nav_profilePicture_Image);
        NavFullName = navView.findViewById(R.id.nav_fullName_TextView);

        UsersReference.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullName"))
                    {
                        String fullName =  dataSnapshot.child("fullName").getValue().toString(); //TODO: Add to user class when added
                        NavFullName.setText(fullName);
                    }

                    if(dataSnapshot.hasChild("profilePicture"))
                    {
                        String image = dataSnapshot.child("profilePicture").getValue().toString(); //TODO: Add to user class when added
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfilePicture);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile does not exist...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendUserToPostAcitvity();
            }
        });

        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts()
    {
       // Query DBquery = PostsReference.orderByChild("fullName");//.equalTo("give");
        //DBquery = DBquery.orderByChild("description").equalTo("here");
        //final Query DBquery = DBquery1 + DBquery2;
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(Posts.class, R.layout.all_posts_layout, PostsViewHolder.class, PostsReference)
        {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
            {
                final String PostKey = getRef(position).getKey();
                viewHolder.setFullName(model.getFullName());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());
                viewHolder.setProfilePicture(getApplicationContext(), model.getProfilePicture());

                viewHolder.setLikeButtonStatus(PostKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                        clickPostIntent.putExtra("PostKey", PostKey);
                        startActivity(clickPostIntent);
                    }
                });

                viewHolder.likeButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        likeCheck = true;

                        LikesReference.addValueEventListener(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (likeCheck.equals(true))
                                {
                                    if (dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        LikesReference.child(PostKey).child(currentUserID).removeValue();
                                        likeCheck = false;
                                    }
                                    else
                                        {
                                        LikesReference.child(PostKey).child(currentUserID).setValue(true);
                                        likeCheck = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }
                });
            }

        };
        postList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton likeButton, commentButton;
        TextView displayNumOfLikes;
        int countLikes;
        String currentUserID;
        DatabaseReference likesRef;

        public PostsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;

            likeButton = mView.findViewById(R.id.likeButton);
            commentButton = mView.findViewById(R.id.commentButton);
            displayNumOfLikes = mView.findViewById(R.id.displayNumOfLikes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikeButtonStatus(final String postKey)
        {
            likesRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    if (dataSnapshot.child(postKey).hasChild(currentUserID))
                    {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeButton.setImageResource((R.drawable.like));
                        displayNumOfLikes.setText(Integer.toString(countLikes) + " Likes");
                    }
                    else
                    {
                        countLikes = (int) dataSnapshot.child(postKey).getChildrenCount();
                        likeButton.setImageResource((R.drawable.dislike));
                        displayNumOfLikes.setText(Integer.toString(countLikes) + " Likes");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }

        public void setFullName(String fullName)
        {
            TextView userName = mView.findViewById(R.id.post_userName);
            userName.setText(fullName);
        }

        public void setProfilePicture(Context ctx, String profilePicture)
        {
            CircleImageView profilePic = mView.findViewById(R.id.post_profilePicture);
            Picasso.with(ctx).load(profilePicture).into(profilePic);
        }

        public void setTime(String time)
        {
            TextView postTime = mView.findViewById(R.id.post_time);
            postTime.setText(" " + time);
        }

        public void setDate(String date)
        {
            TextView postDate = mView.findViewById(R.id.post_date);
            postDate.setText("   " + date);
        }

        public void setDescription(String description)
        {
            TextView postDescription = mView.findViewById(R.id.post_description);
            postDescription.setText(description);
        }

        public void setPostImage(Context ctx, String postImage)
        {
            ImageView postPic = mView.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postImage).into(postPic);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLogInActivity();
        }
        else
        {
            CheckUserExistence();
        }
    }

    private void CheckUserExistence()
    {
        final String currentUserID =  mAuth.getCurrentUser().getUid();

        UsersReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(currentUserID))
                {
                    SendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLogInActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LogInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToPostAcitvity()
    {
       Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
       startActivity(addNewPostIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToFindFriendsActivity()
    {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFirendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);
    }

    // When button to draw up the navigation or menu is selected it will come out
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            case R.id.nav_addNewPost:
            {
                Toast.makeText(this, "Add New Post", Toast.LENGTH_SHORT).show();
                SendUserToPostAcitvity();
            }
            break;

            case R.id.nav_profile:
            {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                SendUserToProfileActivity();
            }
            break;

            case R.id.nav_home:
            {
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_friends:
            {
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_findFriends:
            {
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                SendUserToFindFriendsActivity();
            }
            break;

            case R.id.nav_messeges:
            {
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
            }
            break;

            case R.id.nav_settings:
            {
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                SendUserToSettingsActivity();
            }
            break;

            case R.id.nav_logOut:
            {
                Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                SendUserToLogInActivity();
            }
            break;
        }
    }
}
