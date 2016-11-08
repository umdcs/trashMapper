package edu.umn.trashmapper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        Button button;
        button = (Button) findViewById(R.id.mapTrash);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapTrash();
            }
        });

        button = (Button) findViewById(R.id.mapBins);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapBins();
            }
        });

    }

    private void mapBins(){
        Intent intent = new Intent(this, MapBins.class);
        startActivity(intent);
    }

    private void mapTrash(){
        Intent intent = new Intent(this, TrashDescription.class);
        startActivity(intent);
    }

    private void map(){

    }
}
