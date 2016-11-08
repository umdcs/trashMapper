package edu.umn.trashmapper;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MapBins extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        try
        {
            if (requestCode == PICK_IMAGE)
            {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                photoFile = new File(imgDecodableString);
                //sendMessage(photoFile);
            }
        }
        catch (NullPointerException e)
        {
            toast = Toast.makeText(this, "Invalid picture selected.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private File photoFile = null;
    private static final int PICK_IMAGE=100;
    private Toast toast;
    private String mCurrentPhotoPath;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
}
