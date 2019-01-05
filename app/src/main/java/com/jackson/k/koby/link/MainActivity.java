package com.jackson.k.koby.link;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mainToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.main_page_toolbar);
        drawerLayout = findViewById(R.id.drawable_layout);

        //Setting up the toggle button in the app bar
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Home");
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_closed);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                UserMenuSelector(menuItem);
                return false;
            }
        });
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
            }
            break;

            case R.id.nav_profile:
            {
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
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
            }
            break;

            case R.id.nav_logOut:
            {
                Toast.makeText(this, "Log Out", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }
}
