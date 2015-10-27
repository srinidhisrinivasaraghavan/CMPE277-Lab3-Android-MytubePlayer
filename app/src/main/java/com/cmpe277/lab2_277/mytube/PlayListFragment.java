    package com.cmpe277.lab2_277.mytube;

    import android.support.v4.app.Fragment;
    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.os.Handler;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.MenuItem;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.AdapterView;
    import android.widget.ArrayAdapter;
    import android.widget.CheckBox;
    import android.widget.ImageView;
    import android.widget.ListView;
    import android.widget.TextView;

    import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
    import com.google.api.services.youtube.YouTubeScopes;
    import com.squareup.picasso.Picasso;

    import java.util.ArrayList;
    import java.util.Collections;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Set;

    public class PlayListFragment extends Fragment {


        private ListView videosFound;
        private static final String PREF_ACCOUNT_NAME = "accountName";
        GoogleAccountCredential credential;
        private Handler handler;
        public static PlayListFragment pfragment;

        private List<VideoItem> searchResults;
        private Set<Integer> checkedPos;

        //create instance
        public static PlayListFragment newInstance() {
            Bundle args = new Bundle();
            if(pfragment!=null){
//                pfragment = new PlayListFragment();
                return  pfragment;}
            else
            {
                Log.i("creating","new Play frag");
                pfragment = new PlayListFragment();
                return pfragment;
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_playlist, container, false);

            videosFound= (ListView) rootView.findViewById(R.id.favlist);
            SharedPreferences settings =getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            getActivity().getPreferences(Context.MODE_PRIVATE);
            String user = settings.getString(PREF_ACCOUNT_NAME, null);
            credential =
                    GoogleAccountCredential.usingOAuth2(getActivity(), Collections.singleton(YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_FORCE_SSL + " " + YouTubeScopes.YOUTUBE_READONLY + " " + YouTubeScopes.YOUTUBEPARTNER));
            credential.setSelectedAccountName(user);

            checkedPos = new HashSet<Integer>();
            handler = new Handler();

            addClickListener();
            displayPlayList();
            return rootView;
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle presses on the action bar items
            switch (item.getItemId()) {
                case R.id.action_delete:
                    removeItemsFromPlayList();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
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

        private void removeItemsFromPlayList(){
            GetUsernameTask gut = new GetUsernameTask(getActivity(),SearchFragment.newInstance(), PlayListFragment.newInstance(), credential);
            List<String> plIds = new ArrayList<String>();
            for(Integer p : checkedPos) {
                plIds.add(searchResults.get(p).getPlayListId());
            }
            checkedPos.clear();
            if(!plIds.isEmpty()) {
                gut.execute(3, plIds);
            }
        }
        protected void displayPlayList(){
            GetUsernameTask gut = new GetUsernameTask(getActivity(), SearchFragment.newInstance(), PlayListFragment.newInstance(), credential);
            gut.execute(2);
        }

        protected void updateVideosFound(List<VideoItem> searchResults){
            PlayListFragment.this.searchResults = searchResults;
            Log.i("" + (getActivity() == null), "oooooooooooooo");
            ArrayAdapter<VideoItem> adapter = new ArrayAdapter<VideoItem>(getActivity().getApplicationContext(), R.layout.fragment_playlist, searchResults){
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    if(convertView == null){
                        Log.i("getView","called");
                        convertView = getActivity().getLayoutInflater().inflate(R.layout.fav_list_item, parent, false);
                    }
                    ImageView thumbnail = (ImageView)convertView.findViewById(R.id.fav_thumbnail);
                    TextView title = (TextView)convertView.findViewById(R.id.fav_title);
                //    TextView date =(TextView) convertView.findViewById(R.id.fav_date);
    //              TextView description = (TextView)convertView.findViewById(R.id.fav_description);
                    final CheckBox checkBox=(CheckBox)convertView.findViewById(R.id.fav_checkbox);
                    checkBox.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(checkBox.isChecked()) {
                                checkedPos.add(position);
                            } else {
                                checkedPos.remove(position);
                            }
                        }
                    });
                    VideoItem searchResult = PlayListFragment.this.searchResults.get(position);

                    Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                    title.setText(searchResult.getTitle());
              //      date.setText(searchResult.getDate());
    //                description.setText(searchResult.getDescription());
                    return convertView;
                }
            };
            Log.i("video "+(videosFound==null),"is null");
            Log.i("adapter "+(adapter==null),"is null");

            videosFound.setAdapter(adapter);
        }
    }
