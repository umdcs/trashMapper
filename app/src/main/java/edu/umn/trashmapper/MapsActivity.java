package edu.umn.trashmapper;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        NavigationView.OnNavigationItemSelectedListener,
        AsyncResponse {

    public static final String TAG = MapsActivity.class.getSimpleName();

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */

    /* The instance field for the location function
    */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String LOG_TAG = "MapsActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private Location location;
    private boolean mPermissionDenied = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean check = true;
    private int count = 0;

    /*
     * The instance fields for the map interface
     */
    private ListView mDrawerList;
    //private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;

    /*
     * The instance fields for the marker display
     */

    JSONObject obj;
    JSONArray inter;
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";
    private Marker customMarker;
    private String returned1 = "";
    private Button goToInfo;
    private HashMap<Marker, String> eventMarkerMap;
    private HashMap<Marker, String> infoMarkerMap;
    private ArrayList<Marker> markerList;
    private ArrayList<String> returnedList;
    /*
     * TEST
     */



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpAsyncTask = new HTTPAsyncTask(this);
        setContentView(R.layout.activity_maps);

        //The following are for the map interface
        //mDrawerList = (ListView)findViewById(R.id.navList);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        //mActivityTitle = getTitle().toString();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setUpMapIfNeeded();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        //addDrawerItems();
        //setupDrawer();

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);


        /********** Test ***********/
    /*    Bundle b = getIntent().getExtras();
       // lati = b.getDouble("lat");
       // longi = b.getDouble("long");
        String jsonArray = b.getString("jsonArray");

        try {
            inter = new JSONArray(jsonArray);
           // System.out.println(array.toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/
        restGET(); // Get the trash
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        String sLattitude = String.valueOf(location.getLatitude());
        String sLongitude = String.valueOf(location.getLongitude());

        String s = "Lattitude is " + sLattitude + " Longitude is " + sLongitude;



        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        //    LatLng latLng = new LatLng(lati, longi);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Initial Location!");
        if(count == 0) {
            Toast.makeText(MapsActivity.this, s, Toast.LENGTH_SHORT).show();
            mMap.addMarker(options);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
            count++;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        } else {
            Log.d("DEBUG","granteed last location");
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (location == null) {
            Log.d("UPDATES","When Location is null");
            startLocationUpdates();
            check = false;
        }
        else if(check && count == 0){
            Log.d("UPDATES","When Location is not null");
            handleNewLocation(location);
            count++;
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == PERMISSION_REQUEST_CODE) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //location = null;
                enableMyLocation();
                startLocationUpdates();
                Log.d("Test","Permission granteed");
            }
            else{
                mPermissionDenied = false;
            }

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }



    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        enableMyLocation();
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        //addMarkers();
        //mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        //mMap.setOnMarkerClickListener(this);
        //mMap.setOnInfoWindowClickListener(this);


    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(mPermissionDenied).show(getSupportFragmentManager(), "dialog");
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(this, "Click Info Window", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

            // This causes the marker at Perth to bounce into position when it is clicked.
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(
                            1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker.setAnchor(0.5f, 1.0f + 2 * t);

                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
    private void addMarkers(){
       /* InputStream inputStream = getResources().openRawResource(R.raw.mock_data);
        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();
       try {
           inter = new JSONArray(json);
       }
       catch (JSONException e){
           Log.e("NOTE", "Cannot parse JSON");
           e.printStackTrace();
       }*/
        if(inter == null)
        {
            Log.d("Inter", "inter is null");
        }
        else{
            Log.d("Inter", "inter is not null");
        }
        markerList = new ArrayList<>(inter.length());
        returnedList = new ArrayList<>(inter.length());
        Log.d("Map", "Add Markers");
        try {
            for (int i = 0; i < inter.length(); ++i) {

                Log.d("TEST", inter.toString());
                JSONObject each = inter.getJSONObject(i);
                String userName = "hhhh";//each.getString("user_name");
                //final String trashType = each.getString("type_of_trash");
                String trashType = "organic";
                Double trashLat = each.getDouble("trash_latitude");
                Double trashLong = each.getDouble("trash_longtitude");
                String trashDate = each.getString("trash_generate_date");
                String trashInfo = "";//each.getString("trash_information");
                //String trashPicture = each.getString("picture");
                //String trashPicture = "picture";

                LatLng latLng = new LatLng(trashLat, trashLong);

                //String returned = "User Name: " + userName + "\n" + "Trash Type: " + trashType + "\n"
                //        + "Trash Latitude: " + trashLat + "\n" + "Trash Longitude: " + trashLong + "\n"
                //        + "Trash Date: " + trashDate + "\n" + "%" + trashPicture;/*+ "Trash Info: " + trashInfo*/;

                String returned = "User Name: " + userName + "\n" + "Trash Type: " + trashType + "\n"
                        + "Trash Latitude: " + trashLat + "\n" + "Trash Longitude: " + trashLong + "\n"
                        + "Trash Date: " + trashDate + "\n" + "%" + String.valueOf(i);


                returnedList.add(returned);
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(userName)
                        .snippet("Click on to see more information")
                        //.snippet(returned)
                        .icon(BitmapDescriptorFactory.fromResource(chooseMarker(trashType)));
                //System.out.println("returned is" + returned);

                final Marker customMarker = mMap.addMarker(options);
                markerList.add(customMarker);
                //String updatedName = String.valueOf(customMarker) + String.valueOf(i);

                if(customMarker == null)
                {
                    System.out.println("customMarker is null");
                }
                else{
                    System.out.println("customMarker is not null");
                }
                infoMarkerMap = new HashMap<Marker, String>();

                for(int j = 0; j < markerList.size(); ++j) {
                    infoMarkerMap.put(markerList.get(j), returnedList.get(j));
                }

                if(infoMarkerMap == null)
                {
                    System.out.println("infoMarkerMap is null");
                }
                else{
                    System.out.println("infoMarkerMap is not null");
                }
                //eventMarkerMap = new HashMap<Marker,String>();
                //eventMarkerMap.put(customMarker, trashPicture);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

                mMap.setOnInfoWindowClickListener(
                        new GoogleMap.OnInfoWindowClickListener(){
                            public void onInfoWindowClick(Marker marker){
                                Log.d("MoveCamera","run");
                                //Toast.makeText(getBaseContext(),returned, Toast.LENGTH_SHORT).show();
                               // String pic = eventMarkerMap.get(marker);
                                Intent intent = new Intent(MapsActivity.this, DisplayActivity.class);
                                Bundle extras = new Bundle();
                                //extras.putString("TRASH_INFO", marker.getSnippet()); // put the mpg_message var into bundle
                                //extras.putString("TRASH_INFO", returned);
                                extras.putString("TRASH_INFO", infoMarkerMap.get(marker));
                                //extras.putString("TRASH_PIC_STRING",pic);
                                //System.out.println("returned is " + marker.getSnippet());
                               // System.out.println("returned is: " + returned);
                               // System.out.println("returned String is " + infoMarkerMap.get(marker));
                               // System.out.println("returned Picture is " + eventMarkerMap.get(marker));
                                intent.putExtras(extras);
                                startActivity(intent);
                                //startActivityForResult(intent, 1);

                            }
                        }
                );

            }

        }

        catch (JSONException e)
        {
            e.printStackTrace();
        }

        catch (NullPointerException e)
        {
            Log.d("NULL", "NULL POINTER EXCEPTION");
        }

    }

    private int chooseMarker(String trashType){
        int badge;
        // Use the equals() method on a Marker to check for equals.  Do not use ==.
        if (trashType.equals("organic")) {
            badge = R.drawable.carrot_48;
        } else if (trashType.equals("cans")) {
            badge = R.drawable.tin_can_48;
        } else if (trashType.equals("battery")) {
            badge = R.drawable.charged_battery_50;
        } else if (trashType.equals("plastic")) {
            badge = R.drawable.plastic_50;
        } else if (trashType.equals("paper")) {
            badge = R.drawable.paper_plane_50;
        } else if (trashType.equals("trashcan")){
            badge = R.drawable.trash_can;
        } else {
            // Passing 0 to setImageResource will clear the image view.
            badge = 0;
        }
      //  ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
        return badge;
    }


    /*private void addDrawerItems() {
        String[] itemArray = {"Saved locations", "Others on the map", "Profile"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MapsActivity.this, "HaHaHaHaHa!", Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    /*private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
           /* public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
           /* public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }*/

   // @Override
    /*protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.string.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        /*if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_saved) {
            Intent intent = new Intent(MapsActivity.this, TrashDescription.class);
            startActivity(intent);
        } else if (id == R.id.nav_others) {

        } else if (id == R.id.nav_profile) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    public void restGET()
    {
        httpAsyncTask.execute("http://131.212.144.150:4321/userData", "GET");
        //httpAsyncTask.cancel(true);
        // new HTTPAsyncTask().execute("http://10.0.2.2:4321/userData/userData", "GET");
        // new HTTPAsyncTask().execute("https://lempo.d.umn.edu:8193/userData", "GET");
    }

    HTTPAsyncTask httpAsyncTask;

    @Override
    public void processFinish(String result) {
        try
        {
            JSONObject bjason = new JSONObject(result);
            inter = bjason.getJSONArray("trash");
            JSONObject sjason = inter.getJSONObject(0);
            // Log.d("DEBUG", sjason.getString("longitude"));
            temp = inter.toString();
            Log.d("asdasdasdasdasMAPS", temp);
            addMarkers();

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        httpAsyncTask.cancel(true);
    }
    String temp;
}