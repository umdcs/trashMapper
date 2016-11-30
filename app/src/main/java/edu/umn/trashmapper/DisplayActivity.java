package edu.umn.trashmapper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

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
       try {
           String[] split = message.split("%");
           String normalMessage = split[0];
           //String pictureString = split[1];
           String indexString = split[1];
           index = Integer.parseInt(indexString);
           restGETPhoto();
           //String picture = extra.getString("TRASH_PIC_STRING");
           TextView textView = new TextView(this);
           textView.setTextSize(15);
           textView.setText(normalMessage);

           ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display);
           layout.addView(textView);
       }catch (NullPointerException e){
           e.printStackTrace();
       }

        like = (Button) findViewById(R.id.like);
        dislike = (Button) findViewById(R.id.dislike);
        numberDislike = (TextView) findViewById(R.id.number_dislike);
        numberLike = (TextView) findViewById(R.id.number_like);

        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int l = Integer.parseInt(numberLike.getText().toString());
                l = l + 1;
                numberLike.setText(Integer.toString(l));
            }
        });

        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int l = Integer.parseInt(numberDislike.getText().toString());
                l = l + 1;
                numberDislike.setText(Integer.toString(l));
            }
        });


    }

    private void createFile(String encrypted) throws JSONException
    {
        byte[] imageBytes = new byte[0];
        if(encrypted != null)
        {
            System.out.println("TEST IS " + test);
            Log.d("InCreate", "String is " + encrypted);
            byte[] decoded = Base64.decode(encrypted, DEFAULT);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Bitmap pic = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            pic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = baos.toByteArray();
            pic = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            //Bitmap resized = Bitmap.createScaledBitmap(pic,(int)(pic.getWidth()*0.1), (int)(pic.getHeight()*0.1), true);
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
        httpAsyncTask.execute(httpAsyncTask.address + "/seperate", "GET");
    }

    @Override
    public void processFinish(String result) {
        try
        {
            JSONObject bjason = new JSONObject(result);
            inter = bjason.getJSONArray("items");
            if(index < inter.length())
            obj = inter.getJSONObject(index);
            else
            obj = inter.getJSONObject(inter.length());
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
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.temp);
        linearLayout.setVisibility(View.GONE);
    }

    String pictureString;
    HTTPAsyncTask httpAsyncTask;
    JSONObject obj;
    JSONArray inter;
    int index;
    ImageView image;
    String test = "In createImage";

    private Button like, dislike;
    private TextView numberLike, numberDislike;
}
