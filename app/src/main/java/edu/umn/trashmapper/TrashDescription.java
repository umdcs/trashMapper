package edu.umn.trashmapper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.encodeToString;

public class TrashDescription extends AppCompatActivity implements AsyncResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getUserInformation();
        super.onCreate(savedInstanceState);
        httpAsyncTask = new HTTPAsyncTask(this);

        setContentView(R.layout.activity_trash_description);
        organicCamera = (ImageButton) findViewById(R.id.orgainc);
        paperCamera = (ImageButton) findViewById(R.id.paper);
        plasticCamera = (ImageButton) findViewById(R.id.plastic);
        cansCamera = (ImageButton) findViewById(R.id.cans);
        batteryCamera = (ImageButton) findViewById(R.id.battery);
        Button mapButton = (Button) findViewById(R.id.map_button);
        Button galleryButton = (Button) findViewById(R.id.gallery);
        takePhoto();


        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TrashDescription.this, MapsActivity.class);
                //Bundle b = new Bundle();

                /***** Test ******/
                //b.putDouble("lat", Latitude);
                //b.putDouble("long", Longitude);
                //b.putString("jsonArray",temp);
                //intent.putExtras(b);
                //intent.putExtra("jsonArray",temp);
                httpAsyncTask.cancel(true);
                startActivity(intent);

            }
        });


        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks for permissions to read from storage
                //Requests the permission if it isn't granted and then opens the gallery
                //else opens the gallery
                if (ActivityCompat.checkSelfPermission(TrashDescription.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(TrashDescription.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                } else {
                    openGallery();
                }
            }
        });
    }

    /*
      user take photos when icon button is clicked
     */
    public void takePhoto() {
        paperCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        plasticCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        cansCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        batteryCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        organicCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }

    //get the user's information
    public void getUserInformation() {
        try {
            Intent intent = getIntent();
            userEmail = intent.getStringExtra(UserInformationActivity.USER_NAME);
            userPassword = intent.getStringExtra(UserInformationActivity.USER_PASSWORD);
            Log.d("User Email", userEmail);
            Log.d("User password", userPassword);
        } catch (Exception e) {
            Log.d("user", "failed");
        }
    }


    //opens the gallery after permissions granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: { //Permissions.READ_CONTACTS is an int constant
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                }
            }
        }
    }

    /**
     * This function is used to open the devices camera
     * it creates a new intent that can be started by a
     * button press, and allows users to capture images
     * and then saves them to the devices storage using
     * createImageFile() and stores them to
     * Android/data/edu.umn.trashmapper/files/Pictures"
     * Made following
     * https://developer.android.com/training/camera/photobasics.html
     */
    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Context context = getApplicationContext();
                CharSequence text = "An Error Occurred";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "edu.umn.trashmapper", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * This function creates a file for the picture
     * with the time and day in the filename
     * Made following
     * https://developer.android.com/training/camera/photobasics.html
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    /**
     * Opens the Gallery to pick an image from the
     * internal storage
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }


    /**
     * sets the selected image to photoFile
     * throws NullPointerException if ImageUri is null
     * http://programmerguru.com/android-tutorial/how-to-pick-image-from-gallery/
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                Log.d("StringBefore", imgDecodableString);
                cursor.close();
                photoFile = new File(imgDecodableString);
                //Add photoFile to a list and add id to a list where the id will be consistent with the picture.
                fileList.add(imgDecodableString);
                idList.add(id);
                id++;

                String filePath = photoFile.getAbsolutePath();
                ////////////////lI KUN: track the history location where the user took the photos
                try {
                    ExifInterface exif = new ExifInterface(path);
                    float[] latLong = new float[2];
                    boolean hasLatLong = exif.getLatLong(latLong);

                    Log.d("his", "gps latitude ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
                    Log.d("his", "gps latitude: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));    // 緯度
                    Log.d("his", "gps longitude ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
                    Log.d("his", "gps longitude: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
                    Log.d("his", "gps datetime" +
                            ": " + exif.getAttribute(ExifInterface.TAG_DATETIME));    // 経度
                    trashGenDate = exif.getAttribute(ExifInterface.TAG_DATETIME);
                    trashGenLatitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                    trashGenLongtitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                    trashGenLatitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                    trashGenLongtitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                    fixLocation();

                } catch (Exception e) {
                    e.printStackTrace();

                }

                sendJSONUserInformation();
                sendPictureInformation(photoFile);

            }
        } catch (NullPointerException e) {
            toast = Toast.makeText(this, "Invalid picture selected.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    /**
     * Converts the EXIF Location data to a Double containing the location in degrees
     * http://stackoverflow.com/questions/5269462/how-do-i-convert-exif-long-lat-to-real-values
     */
    private void fixLocation() {

        if (trashGenLatitudeRef.equals("N")) {
            Latitude = convertToDegree(trashGenLatitude);
        } else {
            Latitude = 0 - convertToDegree(trashGenLatitude);
        }

        if (trashGenLongtitudeRef.equals("E")) {
            Longitude = convertToDegree(trashGenLongtitude);
        } else {
            Longitude = 0 - convertToDegree(trashGenLongtitude);
        }

        Log.d("Latitude", Latitude.toString());
        Log.d("Longitude", Longitude.toString());
    }

    private Double convertToDegree(String stringDMS) {
        Double result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Double(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    //Packages the image file in a JSON object and calls restPOST() in it.
    public void sendMessage(File photo) {
        try {
            JSONObject jason = new JSONObject();
            try {
                jason.put("picture", createPhotoString(photo));
            } catch (IOException e) {
                e.printStackTrace();
            }
            restPOST(jason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
    generate the jason object including the user's information and image
    ?????!!! how to implement the user log in function if we actually
    going to need the HTTP request there to verify the password there
    I tried send two HTTP request in two activities
    Crash...crash...and crash...
     */
    public void sendJSONUserInformation() {
        try {
            JSONObject jason = new JSONObject();
            jason.put("type", "UserInformation");
            jason.put("user_name", userEmail);
            jason.put("user_password", userPassword);
            jason.put("type_of_trash", "organic");//testTypeOfTrash());
            jason.put("trash_latitude", Latitude);
            jason.put("trash_longtitude", Longitude);
            jason.put("trash_generate_date", trashGenDate);
            jason.put("trash_information", trashInformation);
            // jason.put("picture", createPhotoString(photo));
            restPOST(jason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendPictureInformation(File photo){
        try{
            JSONObject jason = new JSONObject();
            jason.put("picture", createPhotoString(photo));
            restPOSTPhoto(jason);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*
    return the type of trash
     */
    public String testTypeOfTrash() {
        String returenTypeOfTrash = null;
        if (paper) {
            returenTypeOfTrash = "paper";
        }
        if (organic) {
            returenTypeOfTrash = "organic";
        }
        if (cans) {
            returenTypeOfTrash = "cans";
        }
        if (plastic) {
            returenTypeOfTrash = "plastic";
        }
        if (battery) {
            returenTypeOfTrash = "battery";
        }
        return returenTypeOfTrash;
    }


    //Creates base64 encoded string for JSON storage.
    private String createPhotoString(File photo) throws IOException {
        RandomAccessFile stream = null;
        try {
            stream = new RandomAccessFile(photo, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] photoArray = new byte[0];
        try {
            photoArray = new byte[(int) stream.length()];
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            stream.readFully(photoArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (encodeToString(photoArray, DEFAULT).replaceAll("\n", ""));
    }

    //Gets called when a user clicks on a photo in the gallery.
    public void restPOST(JSONObject jason) {
        //new HTTPAsyncTask().execute("https://lempo.d.umn.edu:8193/userData", "POST", jason.toString());
        //new HTTPAsyncTask().execute("http://10.0.2.2:4321/userData", "POST", jason.toString());
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute("http://131.212.144.150:4321/userData", "POST", jason.toString());
        //httpAsyncTask.cancel(true);
    }

    public void restPOSTPhoto(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute("http://131.212.144.150:4321/seperate", "POST", jason.toString());
    }
    //Creates image file from JSON Object on server.
    private void createFile(String encrypted) throws JSONException {
        if (encrypted != null) {
            Log.d("Debug", "String is " + encrypted);
            byte[] decoded = Base64.decode(encrypted, DEFAULT);
            Bitmap pic = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ImageView image = (ImageView) findViewById(R.id.trash);
            image.setImageBitmap(pic);
        }
    }

    public void restGET() {
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute("http://131.212.144.150:4321/userData", "GET");
        //httpAsyncTask.cancel(true);
        // new HTTPAsyncTask().execute("http://10.0.2.2:4321/userData/userData", "GET");
        // new HTTPAsyncTask().execute("https://lempo.d.umn.edu:8193/userData", "GET");
    }


    //This is the picture that the user takes using the camera.
    private File photoFile = null;
    private static final int PICK_IMAGE = 100;
    private Toast toast;
    /**
     * GPS information
     */
    private String mCurrentPhotoPath;
    private String trashGenDate;
    private String trashGenLatitude;
    private String trashGenLongtitude;
    private String trashGenLatitudeRef;
    private String trashGenLongtitudeRef;
    private Double Latitude = 0.0, Longitude = 0.0;
    /**
     * User's information
     */
    //private String userEmail;
    //private String userPassword;

    private String userEmail;
    private String userPassword;
    /*
    trash type
    */
    private boolean organic;
    private boolean paper;
    private boolean cans;
    private boolean plastic;
    private boolean battery;
    /*
    map the trash bin
     */
    private boolean trashBin;
    /*
    map the trash bin
     */
    private String trashInformation;
    /*
    photo location
     */
    private String latitude;
    private String longtitude;
    /*
     icon buttons
     */
    private ImageButton organicCamera;
    private ImageButton plasticCamera;
    private ImageButton paperCamera;
    private ImageButton cansCamera;
    private ImageButton batteryCamera;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private HTTPAsyncTask httpAsyncTask;

    private ArrayList<String> fileList = new ArrayList<>();
    private ArrayList<Integer> idList = new ArrayList<>();
    private Integer id = 0;

    @Override
    public void processFinish(String output) {

    }
}
