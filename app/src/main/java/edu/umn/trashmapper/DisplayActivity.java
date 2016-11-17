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

import org.json.JSONException;

import static android.util.Base64.DEFAULT;

public class DisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        //Get the values of the the messages in the intent from the MainActivity
        String message = extra.getString("TRASH_INFO");
        String picture = extra.getString("TRASH_PIC_STRING");

        try {
            createFile(picture);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        TextView textView = new TextView(this);
        textView.setTextSize(15);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display);
        layout.addView(textView);
    }

    private void createFile(String encrypted) throws JSONException
    {
        if(encrypted != null)
        {
            Log.d("Debug", "String is " + encrypted);
            byte[] decoded = Base64.decode(encrypted, DEFAULT);
            Bitmap pic = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            ImageView image = (ImageView) findViewById(R.id.trash_display);
            image.setImageBitmap(pic);
        }
    }
}
