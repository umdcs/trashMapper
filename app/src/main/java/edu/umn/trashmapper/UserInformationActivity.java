package edu.umn.trashmapper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInformationActivity extends AppCompatActivity implements AsyncResponse{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        httpAsyncTask = new HTTPAsyncTask(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information);
    }

    public void TransferToTrashDes(View a){
        userNameView=(EditText) findViewById(R.id.log_in_email);
        userPasswordView=(EditText)findViewById(R.id.log_in_password);
        userName=userNameView.getText().toString();
       // String userPassword=userPasswordView.getText().toString();
        try {
            JSONObject jason = new JSONObject();
            jason.put("user_name", userName);
            restPOSTSignIn(jason);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public void restPOSTSignIn(JSONObject jason){
        httpAsyncTask = new HTTPAsyncTask(this);
        httpAsyncTask.execute("https://lempo.d.umn.edu:8193/userPassword", "POST", jason.toString());
    }

    @Override
    public void processFinish(String output) {
        Log.d("get password","hahaha");
        try{
        JSONObject bjason = new JSONObject(output);
        VerifiedPassword = bjason.getString("user_password");
        } catch(JSONException e){
            e.printStackTrace();
        }
        try{
            Log.d("get password",VerifiedPassword);
        }catch(Exception a){
            a.printStackTrace();
        }
        String userPassword=userPasswordView.getText().toString();
        Log.d("current type in",userPassword);
        if(VerifiedPassword.equals(userPassword)){
            Intent intent=new Intent(this, SelectActivity.class);
            intent.putExtra(USER_NAME,userName);
            intent.putExtra(USER_PASSWORD,userPassword);
            startActivity(intent);}
        else{
            Toast.makeText(getApplicationContext(),
                    "please try again", Toast.LENGTH_LONG).show();

        }
    }

    private HTTPAsyncTask httpAsyncTask;
    public EditText userNameView;
    public EditText userPasswordView;
    private String userName;
    private String VerifiedPassword;
    public final static String USER_NAME = "com.example.USER_NAME";
    public final static String USER_PASSWORD = "com.example.USER_PASSWORD";
}
