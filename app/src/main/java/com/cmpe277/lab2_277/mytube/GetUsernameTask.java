package com.cmpe277.lab2_277.mytube;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.List;


public class GetUsernameTask extends AsyncTask {
    Activity mActivity;
    String mScope;
    String mEmail;
    Fragment sFragment, pFragment;
    private Handler handler;
    private int REQUEST_AUTHORIZATION = 11;
    GoogleAccountCredential credential;
    GetUsernameTask(Activity mActivity, Fragment sFragment, Fragment pFragment, GoogleAccountCredential credential) {
        this.credential = credential;
        this.sFragment = sFragment;
        this.pFragment = pFragment;
        this.mActivity = mActivity;
        handler = new Handler();
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
            return credential.getToken();
        } catch (UserRecoverableAuthException userRecoverableException) {
            mActivity.startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
        } catch (Exception e) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        int op = Integer.parseInt(params[0].toString());
     //   Log.i("string value is",""+(params[1]==null));
        try {
            String token = fetchToken();
            if (token != null) {
                YoutubeConnector yc = new YoutubeConnector(mActivity);
                switch (op) {
                    case 1:
                        Log.i("string value inside is",""+(params[1]));
                        final List<VideoItem> searchResults = yc.search(params[1].toString());
                        handler.post(new Runnable() {
                            public void run() {
                                ((SearchFragment) sFragment).updateVideosFound(searchResults);
                            }
                        });
                        break;
                    case 2:
                        final List<VideoItem> playListItems = yc.getVideosFromPlayList();
                        handler.post(new Runnable() {
                            public void run() {
                                ((PlayListFragment) pFragment).updateVideosFound(playListItems);
                            }
                        });
                        break;
                    case 3:
                        List<String> plIDs = (List<String>) params[1];
                        yc.deleteItemsFromPlayList(plIDs);
                        final List<VideoItem> playListItems2 = yc.getVideosFromPlayList();
                        handler.post(new Runnable() {
                            public void run() {
                                ((PlayListFragment) pFragment).updateVideosFound(playListItems2);
                            }
                        });
                        break;
                    case 4:
                        String id = params[1].toString();
                        String desc = params[2].toString();
                        yc.insertItemToPlayList(id, desc);
                        break;
                    default:
                        Intent intent = new Intent(mActivity, TabActivity.class);
                        mActivity.startActivity(intent);
                }
            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
        }
        return null;
    }
}
