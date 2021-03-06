package com.dataart.vyakunin.udacitystudyproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dataart.vyakunin.udacitystudyproject.sync.SunshineSyncAdapter;


public class HomeActivity extends ActionBarActivity implements ItemSelectedCallback {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setIcon(R.drawable.ic_logo);
        getSupportActionBar().setTitle(null);
        if (twoPane = getResources().getBoolean(R.bool.two_pane)) {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().add(R.id.weather_detail_container, DetailFragment.newInstance(null), DetailFragment.TAG).commit();
            }
        }
        SunshineSyncAdapter.initializeSyncAdapter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_show_location) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        String location = sharedPrefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + location + ", no receiving apps installed!");
        }
    }

    @Override
    public void onItemSelected(String date) {
        if (twoPane) {
            getSupportFragmentManager().beginTransaction().replace(R.id.weather_detail_container, DetailFragment.newInstance(date), DetailFragment.TAG).commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).putExtra(DetailActivity.DATE_KEY, date);
            startActivity(intent);
        }
    }



}
