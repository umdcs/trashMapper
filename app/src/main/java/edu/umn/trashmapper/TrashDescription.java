package edu.umn.trashmapper;

import android.Manifest;
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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.util.Base64.DEFAULT;

public class TrashDescription extends AppCompatActivity implements AsyncResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        userName = intent.getStringExtra("user_name");
        if(userName==null){
            userName=intent.getStringExtra("user_name_from_Map");
        }
        super.onCreate(savedInstanceState);
        httpAsyncTask = new HTTPAsyncTask(this);

        setContentView(R.layout.activity_trash_description);
        organicBox = (CheckBox) findViewById(R.id.checkbox_organic);
        paperBox = (CheckBox) findViewById(R.id.checkbox_paper);
        plasticBox = (CheckBox) findViewById(R.id.checkbox_plastic);
        cansBox = (CheckBox) findViewById(R.id.checkbox_cans);
        batteryBox = (CheckBox) findViewById(R.id.checkbox_battery);
        checkBoxes();
        EditText trashEdit=(EditText) findViewById(R.id.reason);
        try{
         trashInfo=trashEdit.getText().toString();}
        catch(Exception e){
            e.printStackTrace();
        }
        Button mapButton = (Button) findViewById(R.id.map_button);
        Button galleryButton = (Button) findViewById(R.id.gallery);
        ImageButton cameraButton = (ImageButton) findViewById(R.id.camera_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(TrashDescription.this, MapsActivity.class);
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

    private void checkBoxes(){
        organicBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                organic = organicBox.isChecked();
            }
        });

        paperBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                paper = paperBox.isChecked();
            }
        });

        plasticBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                plastic = plasticBox.isChecked();
            }
        });

        cansBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cans = cansBox.isChecked();
            }
        });

        batteryBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                battery = batteryBox.isChecked();
            }
        });
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
    private static final int REQUEST_TAKE_PHOTO = 1;

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

    private void getBitmap() {
        try {
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            }
        }
        catch (NullPointerException e){
            toast = Toast.makeText(this, "No image taken or selected.", Toast.LENGTH_SHORT);
            toast.show();
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
        /*
      GPS information
     */
        String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
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
     * @param requestCode is the code that is used to differentiate what intent the data is from
     * @param resultCode Lets you know if the intent ended crrectly
     * @param data includes the data that is returned from the intent
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
                assert cursor != null;
                cursor.moveToFirst();
                String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                photoFile = new File(imgDecodableString);
                //String filePath = photoFile.getAbsolutePath();
                processPhotoFile(path);

            } catch (NullPointerException e) {
                toast = Toast.makeText(this, "Invalid picture selected.", Toast.LENGTH_SHORT);
                toast.show();
            }
            sendJSONUserInformation();
            sendPictureInformation(photoFile);
        }

        else if (requestCode == REQUEST_TAKE_PHOTO  && resultCode == Activity.RESULT_OK) {
            try {
                getBitmap();
                processPhotoFile(photoFile.getAbsolutePath());
            }
            catch (NullPointerException e) {
                toast = Toast.makeText(this, "Invalid picture taken.", Toast.LENGTH_SHORT);
                toast.show();
            }
            sendJSONUserInformation();
            sendPictureInformation(photoFile);
        }
    }

    private void processPhotoFile(String path){
        try {
            ExifInterface exif = new ExifInterface(path);
            float[] latLong = new float[2];

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
        Double result;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = Double.valueOf(stringD[0]);
        Double D1 = Double.valueOf(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = Double.valueOf(stringM[0]);
        Double M1 = Double.valueOf(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = Double.valueOf(stringS[0]);
        Double S1 = Double.valueOf(stringS[1]);
        Double FloatS = S0 / S1;

        result = Double.valueOf(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    /*
    generate the jason object including the user's information and image
    ?????!!! how to implement the user log in function if we actually
    going to need the HTTP request there to verify the password there
    I tried send two HTTP request in two activities
    Crash...crash...and crash...
     */
    private void sendJSONUserInformation() {
        try {
            JSONObject jason = new JSONObject();
            jason.put("type", "UserInformation");
            jason.put("user_name", userName);
            jason.put("type_of_trash", typeOfTrash());
            jason.put("trash_latitude", Latitude);
            jason.put("trash_longtitude", Longitude);
            jason.put("trash_generate_date", trashGenDate);
            jason.put("trash_information", trashInfo);
            restPOST(jason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendPictureInformation(File photo){
        try{
            JSONObject jason = new JSONObject();
            jason.put("picture", createPhotoString(photo));
            restPOSTPhoto(jason);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /*
    return the type of trash
     */
    private String typeOfTrash() {
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

    private String createPhotoString(File photo) {
        byte[] imageBytes;
        String encodedImage;
        Bitmap pic = BitmapFactory.decodeFile(photo.getPath());
        Log.d("Photo.getPath()",photo.getPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.JPEG, 14, baos);
        imageBytes = baos.toByteArray();
        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        Log.d("Picture string", encodedImage);
        return encodedImage;
    }

    //Gets called when a user clicks on a photo in the gallery.
    private void restPOST(JSONObject jason) {
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute(httpAsyncTask.address +  "/userData", "POST", jason.toString());
    }

    private void restPOSTPhoto(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute(httpAsyncTask.address + "/seperate", "POST", jason.toString());
    }

    @Override
    public void processFinish(String output) {}

    //This is the picture that the user takes using the camera.
    private File photoFile = null;
    private static final int PICK_IMAGE = 100;
    private Toast toast;
    private String trashGenDate;
    private String trashGenLatitude;
    private String trashGenLongtitude;
    private String trashGenLatitudeRef;
    private String trashGenLongtitudeRef;
    private Double Latitude = 0.0, Longitude = 0.0;
    private String userName;
    /*trash type*/
    private boolean organic;
    private boolean paper;
    private boolean cans;
    private boolean plastic;
    private boolean battery;
    /*
    map the trash bin
     */
    private String trashInfo;

    private CheckBox organicBox, plasticBox, paperBox, cansBox, batteryBox;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private HTTPAsyncTask httpAsyncTask;
}