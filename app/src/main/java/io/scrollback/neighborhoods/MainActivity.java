package io.scrollback.neighborhoods;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import io.scrollback.library.AuthStatus;
import io.scrollback.library.FollowMessage;
import io.scrollback.library.NavMessage;
import io.scrollback.library.ReadyMessage;
import io.scrollback.library.ScrollbackFragment;
import io.scrollback.library.ScrollbackMessageHandler;

public class MainActivity extends AppCompatActivity implements LocationListener {
    ScrollbackFragment scrollbackFragment = SbFragment.getInstance();
    AreaFragment areaFragment;

    FrameLayout areaFrame;
    FrameLayout sbFrame;
    private LocationManager locationManager;

    public static boolean appOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        areaFrame = (FrameLayout) findViewById(R.id.area_container);
        sbFrame = (FrameLayout) findViewById(R.id.scrollback_container);

        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        scrollbackFragment.setGcmSenderId(getString(R.string.gcm_sender_id));

        scrollbackFragment.setMessageHandler(new ScrollbackMessageHandler() {
            @Override
            public void onNavMessage(final NavMessage message) {
                if (message != null && message.mode != null) {
                    scrollbackFragment.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (message.mode.equals("home")) {
                                showAreaFragment();
                            } else {
                                hideAreaFragment();
                            }
                        }
                    });
                }
            }

            @Override
            public void onAuthMessage(AuthStatus message) {
            }

            @Override
            public void onFollowMessage(FollowMessage message) {
            }

            @Override
            public void onReadyMessage(ReadyMessage message) {
            }
        });

        scrollbackFragment.setPrimaryColor(getResources().getColor(R.color.primary), getResources().getColor(R.color.primary_dark));

        Intent intent = getIntent();
        String action = intent.getAction();
        Uri uri = intent.getData();

        if (intent.hasExtra("scrollback_path")) {
            scrollbackFragment.loadPath(getIntent().getStringExtra("scrollback_path"));
        } else if (Intent.ACTION_VIEW.equals(action) && uri != null) {
            scrollbackFragment.loadUrl(uri.toString());
        } else {
            showAreaFragment();
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.scrollback_container, scrollbackFragment)
                .commit();
    }

    public void showAreaFragment() {
        if (areaFrame.getVisibility() == View.VISIBLE && areaFragment != null) {
            return;
        }

        getSupportActionBar().show();

        areaFragment = AreaFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.area_container, areaFragment)
                .commit();

        areaFrame.setVisibility(View.VISIBLE);
        sbFrame.setVisibility(View.INVISIBLE);

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, this);
        }
    }

    public void hideAreaFragment() {
        if (areaFrame.getVisibility() == View.INVISIBLE && areaFragment == null) {
            return;
        }

        // If you want to customize view animations (like material reveal) look here:
        // https://developer.android.com/training/material/animations.html#Reveal
        getSupportActionBar().hide();

        if (areaFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(areaFragment)
                    .commit();

            areaFragment = null;
        }

        areaFrame.setVisibility(View.INVISIBLE);
        sbFrame.setVisibility(View.VISIBLE);

        locationManager.removeUpdates(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && areaFrame.getVisibility() == View.VISIBLE) {
            finish();

            return true;
        }

        boolean handled = scrollbackFragment.onKeyDown(keyCode, event);

        if (!handled) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();

                return true;
            }

            return super.onKeyDown(keyCode, event);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        appOpen = true;
    }

    /* Request updates at startup */
    @Override
    protected void onResume() {
        super.onResume();

        if (areaFrame.getVisibility() == View.VISIBLE && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, this);
        }
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);

        appOpen = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (areaFragment!= null) {
            areaFragment.setLocation(location);
        }

        locationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
