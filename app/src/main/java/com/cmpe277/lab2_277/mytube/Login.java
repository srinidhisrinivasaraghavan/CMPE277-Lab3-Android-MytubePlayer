package com.cmpe277.lab2_277.mytube;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;

import java.util.Collections;
import java.util.logging.Level;

import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.common.SignInButton;


public class Login extends AppCompatActivity {

    YouTube service;
    //private YouTube youtube;
    private YouTube.Search.List query;

    private static final Level LOGGING_LEVEL = Level.OFF;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    static final int REQUEST_AUTHORIZATION = 1;

    static final int REQUEST_ACCOUNT_PICKER = 2;

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
    String accountName;
    SignInButton login;
    GoogleAccountCredential credential;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        credential =
                GoogleAccountCredential.usingOAuth2(this, Collections.singleton(YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_FORCE_SSL + " " + YouTubeScopes.YOUTUBE_READONLY + " " + YouTubeScopes.YOUTUBEPARTNER));
//        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
//        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
//        service =new YouTube.Builder(httpTransport, jsonFactory, credential)
//                .setApplicationName("MyYouTube").build();
        login = (SignInButton)findViewById(R.id.sign_in_button);
        ((SignInButton) findViewById(R.id.sign_in_button)).setSize(SignInButton.SIZE_WIDE);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseAccount();
            }
        });
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    private void fetchToken() {
        GetUsernameTask gut = new GetUsernameTask(this, SearchFragment.newInstance(), PlayListFragment.newInstance(), credential);
        gut.execute(0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(this, TabActivity.class);
                    startActivity(intent);
                } else {
                    chooseAccount();
                }
                break;

            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = this.getSharedPreferences("user", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        fetchToken();
                    }
                }
                break;
        }
    }
}
