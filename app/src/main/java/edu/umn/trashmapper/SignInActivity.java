package edu.umn.trashmapper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class SignInActivity extends AppCompatActivity implements AsyncResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
    }

    public void TraslateToLogIn(View a){
        userNameView=(EditText) findViewById(R.id.userName);
        userPasswordView=(EditText)findViewById(R.id.userPassword);
        String userName=userNameView.getText().toString();
        String userPassword=userPasswordView.getText().toString();
        try {
            JSONObject jason = new JSONObject();
                jason.put("user_name", userName);
                jason.put("user_password", userPassword );
                restPOSTSignIn(jason);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, UserInformationActivity.class);
        startActivity(intent);
    }

    public void restPOSTSignIn(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute("https://lempo.d.umn.edu:8193/userAccount", "POST", jason.toString());
    }

    @Override
    public void processFinish(String output) {
       // httpAsyncTask.cancel(true);

    }
    private HTTPAsyncTask httpAsyncTask;
    public EditText userNameView;
    public EditText userPasswordView;

}
