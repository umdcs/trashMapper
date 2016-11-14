package edu.umn.trashmapper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        //Get the values of the the messages in the intent from the MainActivity
        String message = extra.getString("TRASH_INFO");
        TextView textView = new TextView(this);
        textView.setTextSize(13);
        textView.setText(message);

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display);
        layout.addView(textView);
    }
}
