package com.jackson.k.koby.link;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Capabilities;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Repeat;
import com.squareup.picasso.Picasso;

import java.security.spec.PSSParameterSpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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

    //Song Stuff
    private DatabaseReference SongsReference, UserRef;
    private RecyclerView currentSongList;
    private static final String CLIENT_ID = "6abd2ef14ec3429abae5c4376d6244bd";
    private static final String REDIRECT_URI = "com.jackson.k.koby.link://callback";

    private StorageReference CurrentSongsImagesReference;

    private String DownloadURL;

    private static SpotifyAppRemote mSpotifyAppRemote;

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;

    private static final String TAG = MainActivity.class.getSimpleName();
    private final ErrorCallback mErrorCallback = throwable -> logError(throwable, "Boom!");

    private final Subscription.EventCallback<PlayerContext> mPlayerContextEventCallback = new Subscription.EventCallback<PlayerContext>() {
        @Override
        public void onEvent(PlayerContext playerContext) {
            //       mPlayerContextButton.setText(String.format(Locale.US, "%s\n%s", playerContext.title, playerContext.subtitle));
            //       mPlayerContextButton.setTag(playerContext);
        }
    };

    @SuppressLint("SetTextI18n")
    private final Subscription.EventCallback<PlayerState> mPlayerStateEventCallback = new Subscription.EventCallback<PlayerState>()
    {
        @Override
        public void onEvent(PlayerState playerState)
        {
            // Get image from track
            mSpotifyAppRemote.getImagesApi()
                    .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                    .setResultCallback(bitmap -> {
                        //mCoverArtImageView.setImageBitmap(bitmap);
                        //mImageLabel.setText(String.format(Locale.ENGLISH, "%d x %d", bitmap.getWidth(), bitmap.getHeight()));

                        UpdateCurrentSong(playerState.track.name, playerState.track.artist.name);//, (Uri)playerState.track.imageUri);
                    });
        }
    };


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

        SongsReference = FirebaseDatabase.getInstance().getReference().child("Songs");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);

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


        //Song Stuff
        currentSongList = findViewById(R.id.allUsersSongs_List);
        currentSongList.setHasFixedSize(true);
        LinearLayoutManager currentSongLinearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        currentSongList.setLayoutManager(currentSongLinearLayoutManager);
        CurrentSongsImagesReference = FirebaseStorage.getInstance().getReference();


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
        DisplayAllUsersSongs();
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

        connect();
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








    private void DisplayAllUsersSongs()
    {
        FirebaseRecyclerAdapter<CurrentSongs, CurrentSongsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<CurrentSongs, CurrentSongsViewHolder>(CurrentSongs.class, R.layout.all_current_song_layout, CurrentSongsViewHolder.class, SongsReference) {
            @Override
            protected void populateViewHolder(CurrentSongsViewHolder viewHolder, CurrentSongs model, int position)
            {
                final String PostKey = getRef(position).getKey();
                viewHolder.setFullName(model.getFullName());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setPostImage(getApplicationContext(), model.getPostImage());
                viewHolder.setProfilePicture(getApplicationContext(), model.getProfilePicture());

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
            }

        };
        currentSongList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }


    public static class CurrentSongsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        String currentUserID;

        public CurrentSongsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setFullName(String fullName)
        {
            TextView userName = mView.findViewById(R.id.currentSong_userName);
            userName.setText(fullName);
        }

        public void setProfilePicture(Context ctx, String profilePicture)
        {
            CircleImageView profilePic = mView.findViewById(R.id.currentSong_profilePicture);
            Picasso.with(ctx).load(profilePicture).into(profilePic);
        }

        public void setTime(String time)
        {
            TextView postTime = mView.findViewById(R.id.currentSong_time);
            postTime.setText(" " + time);
        }

        public void setDate(String date)
        {
            TextView postDate = mView.findViewById(R.id.currentSong_date);
            postDate.setText("   " + date);
        }

        public void setTitle(String title)
        {
            TextView songTitle = mView.findViewById(R.id.currentSong_title);
            songTitle.setText(title);
        }

        public void setPostImage(Context ctx, String postImage)
        {
            ImageView postPic = mView.findViewById(R.id.currentSong_image);
            Picasso.with(ctx).load(postImage).into(postPic);
        }
    }


    private void logError(Throwable throwable, String msg) {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, msg, throwable);
    }

    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_SHORT);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(this, msg, duration).show();
        Log.d(TAG, msg);
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connect() {

     //   SpotifyAppRemote.disconnect(mSpotifyAppRemote);

        SpotifyAppRemote.connect(
               this,
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build(),
                new Connector.ConnectionListener()
                {
                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote)
                    {
                        mSpotifyAppRemote = spotifyAppRemote;
                            if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled())
                            {
                                mPlayerContextSubscription.cancel();
                                mPlayerContextSubscription = null;
                            }


                            mPlayerContextSubscription = (Subscription<PlayerContext>) mSpotifyAppRemote.getPlayerApi()
                                    .subscribeToPlayerContext()
                                    .setEventCallback(mPlayerContextEventCallback)
                                    .setErrorCallback(throwable -> {
                                        logError(throwable, "Subscribed to PlayerContext failed!");
                                    });

                            if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
                                mPlayerStateSubscription.cancel();
                                mPlayerStateSubscription = null;
                            }

                            mPlayerStateSubscription = (Subscription<PlayerState>) mSpotifyAppRemote.getPlayerApi()
                                    .subscribeToPlayerState()
                                    .setEventCallback(mPlayerStateEventCallback)
                                    .setLifecycleCallback(new Subscription.LifecycleCallback() {
                                        @Override
                                        public void onStart() {
                                            logMessage("Event: start");
                                        }

                                        @Override
                                        public void onStop() {
                                            logMessage("Event: end");
                                        }
                                    })
                                    .setErrorCallback(throwable -> {
                                        logError(throwable, "Subscribed to PlayerContext failed!");
                                    });
                        }

                    @Override
                    public void onFailure(Throwable error)
                    {
                        logMessage(String.format("Connection failed: %s", error));
                    }
                });
    }

    private void playUri(String uri) {
        mSpotifyAppRemote.getPlayerApi()
                .play(uri)
                .setResultCallback(empty -> logMessage("Play successful"))
                .setErrorCallback(mErrorCallback);
    }



    private void UpdateCurrentSong(String track, String artist)//, Uri imageURI)
    {
        //StoreImageToDatabase(imageURI);

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
                    postMap.put("title", track + " by " + artist);
                    postMap.put("postImage", DownloadURL);
                    postMap.put("profilePicture", profilePicture);
                    postMap.put("fullName", fullName);

                    SongsReference.child(currentUserID+"CurrentSong").updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(MainActivity.this, "New Post uploaded to Database", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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

    private void StoreImageToDatabase(Uri imageURI)
    {
        String timeStamp = new SimpleDateFormat("_dd-MM-yyyy_HHmmss").format(new Date());

        StorageReference filePath = CurrentSongsImagesReference.child("Current Song Images").child("currentSong" +currentUserID + ".jpg"); //TODO:Maybe re-structure the pictures base off users or add more of a random value so two aren't the same

        filePath.putFile((android.net.Uri)imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DownloadURL = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(MainActivity.this, "Image uploaded to Database", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String message = task.getException().getMessage();
                    Toast.makeText(MainActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
