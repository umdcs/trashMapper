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
        Intent intent = getIntent();
        userName = intent.getStringExtra(UserInformationActivity.USER_NAME);
        userPassword = intent.getStringExtra(UserInformationActivity.USER_PASSWORD);
        //newSignIn   = intent.getExtras().getBoolean(UserInformationActivity.SIGN_IN_CHECK);
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

        button = (Button) findViewById(R.id.map);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SelectActivity.this, MapsActivity.class);
                startActivity(intent);

            }
        });

    }

    private void mapBins(){
        Intent intent = new Intent(this, MapBins.class);
        sendInfo(intent);
        startActivity(intent);
    }

    private void mapTrash(){
        Intent intent = new Intent(this, TrashDescription.class);
        sendInfo(intent);
        startActivity(intent);
    }

    private void sendInfo(Intent intent){
        intent.putExtra(UserInformationActivity.USER_NAME, userName);
        intent.putExtra(UserInformationActivity.USER_PASSWORD, userPassword);
    }
    private String userName;
    private String userPassword;
   // private boolean newSignIn;
}
