package com.example.minhaj.facebooklogin;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "tag";
    private LoginButton loginButton;
    private CallbackManager callbackManager;

    private ImageView imageView;
    private TextView tvName,tvLast,tvEmail,tvGender,tvId;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginButton = (LoginButton) findViewById(R.id.login_btn);
        btn = (Button) findViewById(R.id.btn);
        loginButton.setReadPermissions("public_profile","email");
        imageView = (ImageView) findViewById(R.id.img);
        tvName = (TextView) findViewById(R.id.tv_first_name);
        tvLast = (TextView) findViewById(R.id.tv_last_name);
        tvId = (TextView) findViewById(R.id.tv_id);
        tvEmail = (TextView) findViewById(R.id.tv_email);
        tvGender = (TextView) findViewById(R.id.tv_gender);

        if (userLoggedIn()){
            Log.d(TAG,"logged in");
        }else {
            Log.d(TAG,"logged out");
        }
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG,"success");
                AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG,object.toString());
                        Log.d(TAG,response.toString());

                        updateUi(object);
                    }
                });

                Bundle bundle = new Bundle();
                bundle.putString("fields","id,first_name,last_name,email,birthday,gender,link");
                request.setParameters(bundle);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,"error");
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
    }

    private boolean userLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void updateUi(JSONObject profile) {
        String id = null,fName = null,lName = null,gender = null,email = null;
        URL uri = null;
        try {
            id = profile.getString("id");
            uri = new URL("https://graph.facebook.com/" + id + "/picture?width=300&height=300");
            if (profile.has("first_name")){
                fName = profile.getString("first_name");
            }
            if (profile.has("last_name")){
                lName = profile.getString("last_name");
            }
            if (profile.has("gender")){
                gender = profile.getString("gender");
            }
            if (profile.has("email")){
                email = profile.getString("email");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Glide.with(this).load(uri).into(imageView);
        tvName.setText(fName);
        tvLast.setText(lName);
        tvEmail.setText(email);
        tvGender.setText(gender);
        tvId.setText(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
