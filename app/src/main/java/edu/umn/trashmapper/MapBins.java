package edu.umn.trashmapper;

import android.*;
import android.app.Activity;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.encodeToString;

public class MapBins extends AppCompatActivity implements AsyncResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserInformation();
        setContentView(R.layout.activity_map_bins);
        Button button = (Button) findViewById(R.id.gallery);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Checks for permissions to read from storage
                //Requests the permission if it isn't granted and then opens the gallery
                //else opens the gallery
                if (ActivityCompat.checkSelfPermission(MapBins.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MapBins.this,
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                }
                else {
                    openGallery();
                }
            }
        });
        button = (Button) findViewById(R.id.camera);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        button = (Button) findViewById(R.id.map);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photoFile != null) {
                  //  sendJSONTrashBin();

                }
                Intent intent = new Intent(MapBins.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        recyclingBox = (CheckBox) findViewById(R.id.recycling);
        trashBox = (CheckBox) findViewById(R.id.waste);
        compostBox = (CheckBox) findViewById(R.id.compost);
        checkBoxes();
    }

    private void checkBoxes(){
        recyclingBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(recyclingBox.isChecked())
                {
                    recycling = true;
                }
                else
                {
                    recycling = false;
                }
            }
        });

        compostBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(compostBox.isChecked())
                {
                    compost = true;
                }
                else
                {
                    compost = false;
                }
            }
        });

        trashBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(trashBox.isChecked())
                {
                    trash = true;
                }
                else
                {
                    trash = false;
                }
            }
        });

    }

    public String typeOfTrash() {
        String returenTypeOfTrash = "Not specified";
        if (recycling) {
            returenTypeOfTrash = "recycling";
        }
        if (compost) {
            returenTypeOfTrash = "compost";
        }
        if (trash) {
            returenTypeOfTrash = "waste";
        }
        return returenTypeOfTrash;
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
            }
            catch (IOException ex) {
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

    private void getBitmap() {
        try {
            if (photoFile.exists()) {
                bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                //ImageView imageView = (ImageView) findViewById(R.id.imageView);
                //imageView.setImageBitmap(bitmap);
            }
        }
        catch (NullPointerException e){
            toast = Toast.makeText(this, "No image taken or selected.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
    /**
     * sets the selected image to photoFile
     * throws NullPointerException if ImageUri is null
     * http://programmerguru.com/android-tutorial/how-to-pick-image-from-gallery/
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
                try {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                photoFile = new File(imgDecodableString);
                String filePath = photoFile.getAbsolutePath();
                processPhotoFile(path);

             } catch (NullPointerException e) {
                    toast = Toast.makeText(this, "Invalid picture selected.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                sendJSONTrashBin();
                sendPictureInformation(photoFile);
            }

            else if (requestCode == REQUEST_TAKE_PHOTO  && resultCode == Activity.RESULT_OK) {
                try {
                    getBitmap();
                    processPhotoFile(photoFile.getAbsolutePath());
                } catch (NullPointerException e) {
                    toast = Toast.makeText(this, "Invalid picture taken.", Toast.LENGTH_SHORT);
                    toast.show();
                }
                sendJSONTrashBin();
                sendPictureInformation(photoFile);
            }
    }

    private void processPhotoFile(String path){
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
    }

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

    public void sendJSONTrashBin(/*File photo*/) {
        try {
            JSONObject jason = new JSONObject();
            /*
              send the trash bin's location
              send the day when users find that trash bin
              send the picture of the trash bin
             */
            /*the MapActivity needs the server to send two arrays back
            one is the trash bin array (pin all the trash bins on the map)
            one is the userInformation array (pin all the users' data on the map(share between friends))
             */
                jason.put("type", "UserInformation");
                jason.put("user_name", userEmail);
                jason.put("user_password", userPassword);
                jason.put("type_of_trash", typeOfTrash());
                jason.put("trash_latitude", Latitude);
                jason.put("trash_longtitude", Longitude);
                jason.put("trash_generate_date", trashGenDate);
                //jason.put("picture", createPhotoString(photo));

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
    private void restPOST(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);

        httpAsyncTask.execute("https://lempo.d.umn.edu:8193/userData", "POST", jason.toString());
    }

    public void restPOSTPhoto(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);
        //httpAsyncTask.execute("http://192.168.1.19:4321/seperate", "POST", jason.toString());
        httpAsyncTask.execute("http://lempo.d.umn.edu:8193/seperate", "POST", jason.toString());

    }

    private String createPhotoString(File photo) throws IOException {

        byte[] imageBytes = new byte[0];
        String encodedImage = "";
            try {
                Bitmap pic = BitmapFactory.decodeFile(photo.getPath());
                Log.d("Photo.getPath()", photo.getPath());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                pic.compress(Bitmap.CompressFormat.JPEG, 14, baos);
                imageBytes = baos.toByteArray();
                encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            } catch (NullPointerException e){
                toast = Toast.makeText(this, "Take another picture.", Toast.LENGTH_SHORT);
                toast.show();
            }
        return encodedImage;
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

    private File photoFile = null;
    private Bitmap bitmap;
    private static final int PICK_IMAGE=100;
    private Toast toast;
    private HTTPAsyncTask httpAsyncTask = new HTTPAsyncTask(this);
    private CheckBox recyclingBox, compostBox, trashBox;
    private boolean recycling, compost, trash;
    private String encoded;
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

    private String userEmail;
    private String userPassword;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    public void processFinish(String output) {

    }
}
