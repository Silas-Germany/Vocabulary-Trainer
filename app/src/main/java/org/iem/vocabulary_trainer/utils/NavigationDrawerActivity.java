package org.iem.vocabulary_trainer.utils;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.training.TrainingView;

public class NavigationDrawerActivity extends AppCompatActivity {
    private static final String LOG_TAG = "VT_" + NavigationDrawerActivity.class.getSimpleName();

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        // initialize the action-bar: with navigation-drawer-icon and without title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        // initialize the navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                itemClicked(item.getItemId());
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // initialize the first fragment
        if (getSupportFragmentManager() == null) return;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new TrainingView())
                .commit();
    }
    // initialize the ActionBarDrawerToggle
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event, otherwise call superior class
        // (and pass down the call through this to the fragment function)
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // what to do, if an item of the navigation drawer is clicked
    private void itemClicked(int which) {
        Log.d(LOG_TAG, "Item clicked");
        switch (which) {
            case R.id.item_training:
                if (getSupportFragmentManager() == null) return;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, new TrainingView())
                        .commit();
        }
    }

    // if a fragment was changed from inside a fragment, this method has to be called
    public void updateItem() {
        Fragment actualFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (actualFragment instanceof TrainingView) {
            NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_drawer);
            navigationView.setCheckedItem(R.id.item_training);
        }
        minimizeKeyboard();
        Log.d(LOG_TAG, "Selected item updated");
    }

    // minimize keyboard if open
    public void minimizeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        Log.d(LOG_TAG, "Keyboard manually minimized");
    }
}
