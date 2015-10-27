package com.cmpe277.lab2_277.mytube;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.youtube.YouTubeScopes;
import com.squareup.picasso.Picasso;

public class SearchFragment extends Fragment {

    private EditText searchInput;
    private ListView videosFound;
    public static SearchFragment sfragment;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    GoogleAccountCredential credential;

    private Handler handler;

    private List<VideoItem> searchResults;

    public static SearchFragment newInstance() {
        Bundle args = new Bundle();
        if(sfragment!=null)
            return  sfragment;
        else
        {
            Log.i("creating","new search frag");
            sfragment = new SearchFragment();
            return sfragment;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        SharedPreferences settings =getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        getActivity().getPreferences(Context.MODE_PRIVATE);
        String user = settings.getString(PREF_ACCOUNT_NAME, null);
        credential =
                GoogleAccountCredential.usingOAuth2(getActivity(), Collections.singleton(YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_FORCE_SSL + " " + YouTubeScopes.YOUTUBE_READONLY + " " + YouTubeScopes.YOUTUBEPARTNER));
        credential.setSelectedAccountName(user);

        searchInput = (EditText)rootView.findViewById(R.id.search_input);

        videosFound = (ListView)rootView.findViewById(R.id.videos_found);
        Log.i("mapping here ","views");
        handler = new Handler();

        addClickListener();

        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Log.i("reaching done button",":)");
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                return true;
            }
        });
return rootView;
    }


    private void loadPlayList() {
        Intent intent = new Intent(getActivity().getApplicationContext(), PlayListFragment.class);
        startActivity(intent);
    }

    private void addClickListener(){
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayerActivity.class);
                intent.putExtra("VIDEO_ID", searchResults.get(pos).getId());
                startActivity(intent);
            }

        });
    }

    private void searchOnYoutube(final String keywords){
        GetUsernameTask gut = new GetUsernameTask(getActivity(), SearchFragment.newInstance(),PlayListFragment.newInstance() , credential);
        Log.i("i am reaching ", "");
        gut.execute(1, keywords);
    }

    private void addToPlayList(String id, String desc){
        GetUsernameTask gut = new GetUsernameTask(getActivity(), SearchFragment.newInstance(), PlayListFragment.newInstance(), credential);
        gut.execute(4, id, desc);
        PlayListFragment pf = PlayListFragment.newInstance();
        pf.displayPlayList();
    }
    private void removeFromPlayList(String id, String description) {
    }
    protected void updateVideosFound(List<VideoItem> searchResults) {
        SearchFragment.this.searchResults = searchResults;
        Log.i("raching to update",";)");
        ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(), R.layout.video_item, searchResults) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                Log.i("getView","called");
                if (convertView == null) {
                    convertView = getActivity().getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.video_title);
                TextView date=(TextView) convertView.findViewById(R.id.video_date);
                TextView count =(TextView)convertView.findViewById(R.id.view_count);
                //TextView description = (TextView) convertView.findViewById(R.id.video_description);
                final RatingBar ratingBar = (RatingBar) convertView.findViewById(R.id.rating);
                VideoItem searchResult = SearchFragment.this.searchResults.get(position);
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    @Override
                    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                        if (ratingBar.getRating() > 0.0F) {
                            VideoItem searchResult = SearchFragment.this.searchResults.get(position);
                            addToPlayList(searchResult.getId(), searchResult.getTitle());
//                            loadPlayList();
                        } else {
                            ratingBar.setRating(1.0F);
                        }
                    }
                });
                ratingBar.setFocusable(false);
                ratingBar.setFocusableInTouchMode(false);
                    Log.i("title","title!!!!!");
                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                date.setText(searchResult.getDate());
                count.setText(searchResult.getViewCount());
                //description.setText(searchResult.getDescription());
                return convertView;
            }
        };

        Log.i("video "+(videosFound==null),"is null");
        Log.i("adapter "+(adapter==null),"is null");
        videosFound.setAdapter(adapter);
    }

    public Fragment getSearchFragment()
    {
        return sfragment;
    }
}