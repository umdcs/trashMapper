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
        //getUserInformation();
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
        //takePhoto();
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
                //Bundle b = new Bundle();

                /***** Test ******/
                //b.putDouble("lat", Latitude);
                //b.putDouble("long", Longitude);
                //b.putString("jsonArray",temp);
                //intent.putExtras(b);
                //intent.putExtra("jsonArray",temp);
                httpAsyncTask.cancel(true);
                //unTint();
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
                if(organicBox.isChecked())
                {
                    organic = true;
                }
                else
                {
                    organic = false;
                }
            }
        });

        paperBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(paperBox.isChecked())
                {
                    paper = true;
                }
                else
                {
                    paper = false;
                }
            }
        });

        plasticBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(plasticBox.isChecked())
                {
                    plastic = true;
                }
                else
                {
                    plastic = false;
                }
            }
        });

        cansBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(cansBox.isChecked())
                {
                    cans = true;
                }
                else
                {
                    cans = false;
                }
            }
        });

        batteryBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(batteryBox.isChecked())
                {
                    battery = true;
                }
                else
                {
                    battery = false;
                }
            }
        });
    }

    //get the user's information
    /*public void getUserInformation() {
        try {
            Intent intent = getIntent();
            userEmail = intent.getStringExtra(UserInformationActivity.USER_NAME);
            userPassword = intent.getStringExtra(UserInformationActivity.USER_PASSWORD);
            Log.d("User Email", userEmail);
            Log.d("User password", userPassword);
        } catch (Exception e) {
            Log.d("user", "failed");
        }
    }*/


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
            sendJSONUserInformation();
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
            sendJSONUserInformation();
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
            jason.put("user_name", userName);
            //jason.put("user_password", userPassword);
            jason.put("type_of_trash", typeOfTrash());
            jason.put("trash_latitude", Latitude);
            jason.put("trash_longtitude", Longitude);
            jason.put("trash_generate_date", trashGenDate);
            jason.put("trash_information", trashInfo);
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
    public String typeOfTrash() {
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


    private String createPhotoString(File photo) throws IOException {
        byte[] imageBytes = new byte[0];
        String encodedImage = "";
        Bitmap pic = BitmapFactory.decodeFile(photo.getPath());
        Log.d("Photo.getPath()",photo.getPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pic.compress(Bitmap.CompressFormat.JPEG, 14, baos);
        imageBytes = baos.toByteArray();
        encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    //Gets called when a user clicks on a photo in the gallery.
    public void restPOST(JSONObject jason) {
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute(httpAsyncTask.address +  "/userData", "POST", jason.toString());

    }

    public void restPOSTPhoto(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute(httpAsyncTask.address + "/seperate", "POST", jason.toString());

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
        httpAsyncTask.execute(httpAsyncTask.address + "/userData", "GET");

    }



    //This is the picture that the user takes using the camera.
    private File photoFile = null;
    private static final int PICK_IMAGE = 100;
    private Toast toast;
    private Bitmap bitmap;
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
    /**
     * User's information
     */
    //private String userEmail;
    //private String userPassword;

    private String userName;
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
    private String trashInfo;
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

    private CheckBox organicBox, plasticBox, paperBox, cansBox, batteryBox;
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private HTTPAsyncTask httpAsyncTask;


    @Override
    public void processFinish(String output) {

    }
}