package edu.umn.trashmapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import static android.util.Base64.DEFAULT;
public class DisplayActivity extends AppCompatActivity implements AsyncResponse
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        httpAsyncTask = new HTTPAsyncTask(this);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        //Get the values of the the messages in the intent from the MapsActivity
        String message = extra.getString("TRASH_INFO");
        orientation = extra.getInt("TRASH_ORIENTATION");
        TextView info = (TextView) findViewById(R.id.info);
        System.out.println("message is " + message);
        try
        {
            String[] split = message.split("%");
            String normalMessage = split[0];
            String indexString = split[1];
            index = Integer.parseInt(indexString);
            restGETPhoto();
            info.setTextSize(15);
            info.setText(normalMessage);
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
        }
        like = (Button) findViewById(R.id.like);
        dislike = (Button) findViewById(R.id.dislike);
        numberDislike = (TextView) findViewById(R.id.number_dislike);
        numberLike = (TextView) findViewById(R.id.number_like);
        numberLike.setText(Integer.toString(likes));
        numberDislike.setText(Integer.toString(dislikes));
        like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //if(opinion == false)
                //{
                opinion = true;
                likes = likes + 1;
                numberLike.setText(Integer.toString(likes));
                JSONObject jason = new JSONObject();
                try
                {
                    jason.put("trash_likes", likes);
                    jason.put("trash_dislikes", dislikes);
                    jason.put("trash_index", index);
                    restPOST(jason);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            //}
        });
        dislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //if(opinion == false)
                //{
                if(likes - dislikes == -3)
                {
                    JSONObject jason = new JSONObject();
                    try
                    {
                        jason.put("trash_index", index);
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                    restDELETE(jason);
                    finish();
                }
                opinion = true;
                dislikes = dislikes + 1;
                numberDislike.setText(Integer.toString(dislikes));
                JSONObject jason = new JSONObject();
                try
                {
                    jason.put("trash_likes", likes);
                    jason.put("trash_dislikes", dislikes);
                    jason.put("trash_index", index);
                    restPOST(jason);
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
                //}
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
            Matrix matrix = new Matrix();
            if(orientation == 3)
            {
                matrix.postRotate(180);
            }
            if(orientation == 6)
            {
                matrix.postRotate(90);
            }
            if(orientation == 8)
            {
                matrix.postRotate(270);
            }
            Bitmap rpic = Bitmap.createBitmap(pic, 0, 0, pic.getWidth(), pic.getHeight(), matrix, true);
            rpic.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageBytes = baos.toByteArray();
            rpic = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            ImageView image = (ImageView) findViewById(R.id.trash);
            image.setImageBitmap(rpic);
        }
        else{
            Log.d("NULL","String is null");
        }
    }
    private void restPOST(JSONObject jason)
    {
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute(httpAsyncTask.address + "/opinion", "POST", jason.toString());
    }
    public void restGETPhoto()
    {
        httpAsyncTask.execute(httpAsyncTask.address + "/seperate", "GET");
    }
    public void restDELETE(JSONObject jason)
    {
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute(httpAsyncTask.address + "/userData", "DELETE", jason.toString());
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
            pictureString = obj.getString("picture");
            int rlikes = obj.getInt("trash_likes");
            int rdislikes = obj.getInt("trash_dislikes");
            numberLike.setText(Integer.toString(rlikes));
            numberDislike.setText(Integer.toString(rdislikes));
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
    String test = "In createImage";
    private boolean opinion = false;
    private int orientation;
    private int likes = 0;
    private int dislikes = 0;
    private Button like;
    private Button dislike;
    private int index;
    private TextView numberLike, numberDislike;
}