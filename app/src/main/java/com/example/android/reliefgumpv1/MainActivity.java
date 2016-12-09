package com.example.android.reliefgumpv1;

import android.Manifest;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private Button startButton;
    private ViewFlipper viewFlipper;
    private ViewFlipper viewFlipper2;
    private Button pauseButton;
    private Button resumeButton;
    private Button stopButton;
    private Countup countup;
    private Handler handler;
    private TextView duration;
    private GoogleApiClient mgoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int PERM_FOR_LOCATION = 0;
    private MapFragment mMapFragment;
    private Location currentLocation;
    private FragmentTransaction fragmentTransaction;
    private boolean enable;
    private Location firstLocation;
    private ArrayList<LatLng> locationList;
    private Polyline polyline;
    private float distanceFromMap;
    private TextView distanceToDisplay;
    private Boolean isDone = false;
    private Location tempLocation;
    private Boolean isStopped;//TODO:remove this variable


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationList = new ArrayList<LatLng>();
        distanceToDisplay = (TextView) findViewById(R.id.distance);
        enable = true;
        handler = new Handler();
        duration = (TextView) findViewById(R.id.duration);
        countup = new Countup(handler) {
            @Override
            public void updateUI(long timeElapsed) {
                duration.setText(Countup.convertToString(timeElapsed));
            }
        };
        //Animation Variables
        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        Animation in2 = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out2 = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        //ViewFLipper variables
        viewFlipper = (ViewFlipper) findViewById(R.id.first_viewflipper);
        viewFlipper.setInAnimation(in);
        viewFlipper.setOutAnimation(out);
        viewFlipper.setDisplayedChild(R.id.first_layout);
        viewFlipper2 = (ViewFlipper) findViewById(R.id.second_viewflipper);
        viewFlipper2.setInAnimation(in2);
        viewFlipper2.setOutAnimation(out2);
        viewFlipper2.setDisplayedChild(R.id.pause_button);

        //Button declarations
        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopped = false;//TODO:remove this variable
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(getParent(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_FOR_LOCATION);
                    return;
                }
                viewFlipper.showNext();
                countup.start();
                firstLocation = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
                Log.i("VJ", "First Location : " + firstLocation);
                mMapFragment = MapFragment.newInstance();
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(5000);
                LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mLocationRequest, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        currentLocation = location;
                        locationList.add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        double d = convertMtoKM(calculateDistance(currentLocation));
                        displayFn(d);
                        Log.i("VJ","distance to display"+ d );
                        mMapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {

                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 13)));
                                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    googleMap.setMyLocationEnabled(enable);
                                    return;
                                }
                                googleMap.setMyLocationEnabled(enable);
                                googleMap.addMarker(new MarkerOptions().position(new LatLng(firstLocation.getLatitude(), firstLocation.getLongitude())).title("Start"));
                                googleMap.addPolyline(new PolylineOptions().addAll(new Iterable<LatLng>() {
                                    @Override
                                    public Iterator<LatLng> iterator() {
                                        return locationList.iterator();
                                    }
                                }).color(0xFFB71C1C));

                            }
                        });
                        if(isStopped)
                        {
                            //TODO: remove this method later on
                            LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient,this);
                        }
                    }

                });

                fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container_linear, mMapFragment, "VJ_MAP_FRAGMENT");
                fragmentTransaction.commit();
            }
        });
        pauseButton = (Button) findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper2.showNext();
                countup.pause();
            }
        });
        resumeButton = (Button) findViewById(R.id.resume_button);
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFlipper2.showNext();
                countup.start();
            }
        });
        stopButton = (Button) findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationList.clear();
                duration.setText("00:00:00");
                distanceToDisplay.setText("0.0");
                viewFlipper.showNext();
                viewFlipper2.showNext();
                countup.stop();
                isStopped = true;//TODO:remove this variable

            }
        });

        //GoogleApiClient declaration
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //CONVERT METERS TO KM
    public float convertMtoKM(float f)
    {
        return f*0.001f;
    }
    //To display the kilometer distance
    public void displayFn(Double d)
    {
        if(d<1)
        {
            distanceToDisplay.setText("0." + new DecimalFormat("#").format(d));
        }
        else
        {
            distanceToDisplay.setText(new DecimalFormat("#.#").format(d));
        }
    }

    //TO CALCULATE GROUND DISTANCE
    public float calculateDistance(Location location)
    {
        //For first Calculation
        if(!isDone)
        {
            distanceFromMap += firstLocation.distanceTo(location);
            isDone = true;
            tempLocation = location;
        }
        else
        {

            distanceFromMap += tempLocation.distanceTo(location);
            tempLocation = location;

        }
        return distanceFromMap;
    }



    @Override
    protected void onStart() {
        super.onStart();
        mgoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mgoogleApiClient.isConnected()) {
            mgoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_FOR_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERM_FOR_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERM_FOR_LOCATION);
                        return;
                    }
                    LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("VJ", "onConnection Suspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("VJ", "Connection Failed");
    }
}
