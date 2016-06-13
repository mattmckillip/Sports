/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.matt.movieWatchList.viewControllers.activities.shows;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.matt.movieWatchList.MyApplication;
import com.example.matt.movieWatchList.R;
import com.example.matt.movieWatchList.viewControllers.activities.SettingsActivity;
import com.example.matt.movieWatchList.viewControllers.activities.movies.BrowseMoviesActivity;
import com.example.matt.movieWatchList.viewControllers.fragments.movies.MovieWatchListFragment;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;


/**
 * Provides UI for the main screen.
 */
public class TVShowWatchListActivity extends AppCompatActivity {
    private static final String TAG = TVShowWatchListActivity.class.getSimpleName();
    Adapter adapterViewPager;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate realms
        RealmConfiguration config1 = new RealmConfiguration.Builder(this)
                .name("default")
                .schemaVersion(6)
                .migration(new RealmMigration() {
                    @Override
                    public long execute(Realm realm, long version) {
                        return 6;
                    }
                })
                .build();

        Realm.setDefaultConfiguration(config1);
        Realm uiRealm =  Realm.getInstance(getApplicationContext());

        ((MyApplication) this.getApplication()).setUiRealm(uiRealm);

        // Adding Toolbar to Main screen
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Movie Watch List");
        applyFontForToolbarTitle(this);

        // Setting ViewPager for each Tabs
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        try {
            tabs.getTabAt(0).setIcon(R.drawable.ic_dvr_white_24dp);
            tabs.getTabAt(1).setIcon(R.drawable.ic_playlist_add_check_white_24dp);
        } catch(NullPointerException npe) {
            //pass
        }

        // Create Navigation drawer and inlfate layout
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        // Adding menu icon to Toolbar
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    // This method will trigger on item Click of navigation menu
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // Set item in checked state
                        menuItem.setChecked(true);
                        Adapter adapter = new Adapter(getSupportFragmentManager());
                        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);


                        //Check to see which item was being clicked and perform appropriate action
                        switch (menuItem.getItemId()) {

                            //Replacing the main content with ContentFragment


                            case R.id.movie_watch_list_menu_item:
                                mDrawerLayout.closeDrawers();
                                return true;

                            case R.id.movie_browse_menu_item:
                                Snackbar.make(getCurrentFocus(), "MovieQueryReturn",
                                        Snackbar.LENGTH_LONG).show();

                                Intent i = new Intent(TVShowWatchListActivity.this, BrowseMoviesActivity.class);
                                startActivity(i);
                                return true;

                            case R.id.movie_search_menu_item:
                                Snackbar.make(getCurrentFocus(), "MovieQueryReturn",
                                        Snackbar.LENGTH_LONG).show();

                                Intent searchIntent = new Intent(TVShowWatchListActivity.this, SearchTVShowsActivity.class);
                                startActivity(searchIntent);
                                return true;

                            case R.id.tv_browse_menu_item:
                                Intent browseTVShowsIntent = new Intent(TVShowWatchListActivity.this, BrowseTVShowsActivity.class);
                                startActivity(browseTVShowsIntent);
                                return true;

                            case R.id.settings_menu_item:
                                Intent settingsIntent = new Intent(TVShowWatchListActivity.this, SettingsActivity.class);
                                startActivity(settingsIntent);
                                return true;
                        }




                        // Closing drawer on item click
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });

        // Adding Floating Action Button to bottom right of main view
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final Intent intent = new Intent(this, BrowseMoviesActivity.class);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(intent);
            }
        });

        /*TextView navHeaderText = (TextView) mDrawerLayout.findViewById(R.id.nav_header_text);
        Typeface font = Typeface.
                createFromAsset(this.getAssets(), "fonts/Lobster-Regular.ttf");
        navHeaderText.setTypeface(font);*/
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        adapterViewPager = new Adapter(getSupportFragmentManager());

        Bundle watchedMoviesBundle = new Bundle();
        watchedMoviesBundle.putInt("watched", 1);
        MovieWatchListFragment watchedMovies = new MovieWatchListFragment();
        watchedMovies.setArguments(watchedMoviesBundle);

        Bundle watchListMoviesBundle = new Bundle();
        watchListMoviesBundle.putInt("watched", 0);
        MovieWatchListFragment watchListMovies = new MovieWatchListFragment();
        watchListMovies.setArguments(watchListMoviesBundle);

        adapterViewPager.addFragment(watchListMovies, " Watch List");
        adapterViewPager.addFragment(watchedMovies, " Watched");
        viewPager.setAdapter(adapterViewPager);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Log.d("onOptionsItemSelected()", "settings");

                return true;

            case R.id.action_sort:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Log.d("onOptionsItemSelected()", "Sort");

                return true;

            case android.R.id.home:
                // User chose the "Favorite" action, mark the current item
                // as a favorite...
                Log.d("onOptionsItemSelected()", "Sort");
                mDrawerLayout.openDrawer(GravityCompat.START);

                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }

        /*int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);*/
    }

    @Override
    public void onBackPressed() {
        if (this.mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static void applyFontForToolbarTitle(Activity context){
        Toolbar toolbar = (Toolbar) context.findViewById(R.id.toolbar);
        for(int i = 0; i < toolbar.getChildCount(); i++){
            View view = toolbar.getChildAt(i);
            if(view instanceof TextView){
                TextView tv = (TextView) view;
                Typeface titleFont = Typeface.
                        createFromAsset(context.getAssets(), "fonts/Lobster-Regular.ttf");
                if(tv.getText().equals(context.getTitle())){
                    tv.setTypeface(titleFont);
                    break;
                }
            }
        }
    }
}