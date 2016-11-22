package edu.umn.trashmapper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.util.Base64.DEFAULT;

public class DisplayActivity extends AppCompatActivity implements AsyncResponse {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        httpAsyncTask = new HTTPAsyncTask(this);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        //Get the values of the the messages in the intent from the MapsActivity
        String message = extra.getString("TRASH_INFO");
        System.out.println("message is " + message);
        String[] split = message.split("%");
        String normalMessage = split[0];
        //String pictureString = split[1];
        String indexString = split[1];
        index = Integer.parseInt(indexString);
       /* if(inter == null)
        {
            Log.d("Inter", "inter is null");
        }
        else{
            Log.d("Inter", "inter is not null");
        }
        try {
            obj = inter.getJSONObject(index);
            pictureString = obj.getString("picture");
        } catch (JSONException e) {
            e.printStackTrace();
        } *//*catch (NullPointerException e)
        {
            Log.d("NULL", "NULL POINTER EXCEPTION");
        }*/

        /*if(pictureString == null)
        {
            Log.d("pictureString", pictureString);
        }
        else{
            Log.d("pictureString","NOTNULL");
        }*/
        // Log.d("DEBUG", sjason.getString("longitude"));

        restGETPhoto();
        //String picture = extra.getString("TRASH_PIC_STRING");

        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setText(normalMessage);

       // image = new ImageView(this);

        Log.d("Before", "Haha");
        /*try {
            createFile(pictureString);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        Log.d("After", "HaHahaha");
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display);
        layout.addView(textView);
        //layout.addView(image);

        Log.d("Final", "Xixixi");
    }

    private void createFile(String encrypted) throws JSONException
    {
        if(encrypted != null)
        {
            System.out.println("TEST IS " + test);
            Log.d("InCreate", "String is " + encrypted);
            byte[] decoded = Base64.decode(encrypted, DEFAULT);
            Bitmap pic = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ImageView image = (ImageView) findViewById(R.id.trash);
            image.setImageBitmap(pic);
            //image.setImageResource(R.drawable.carrot_48);
        }
        else{
            Log.d("NULL","String is null");
        }
    }

    public void restGETPhoto()
    {
        httpAsyncTask.execute("http://131.212.144.150:4321/seperate", "GET");
        //httpAsyncTask.cancel(true);
        // new HTTPAsyncTask().execute("http://10.0.2.2:4321/userData/userData", "GET");
        // new HTTPAsyncTask().execute("https://lempo.d.umn.edu:8193/userData", "GET");
    }

    @Override
    public void processFinish(String result) {
        try
        {
            JSONObject bjason = new JSONObject(result);
            inter = bjason.getJSONArray("items");
            obj = inter.getJSONObject(index);
            // Log.d("DEBUG", sjason.getString("longitude"));
            pictureString = obj.getString("picture");
            Log.d("asdasdasdasdasDISP", pictureString);

            try {
                createFile(pictureString);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        httpAsyncTask.cancel(true);
    }

    String pictureString;
    HTTPAsyncTask httpAsyncTask;
    JSONObject obj;
    JSONArray inter;
    int index;
    ImageView image;
    String test;
}
