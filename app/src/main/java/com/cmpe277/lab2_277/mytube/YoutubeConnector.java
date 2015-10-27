package com.cmpe277.lab2_277.mytube;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeScopes;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.VideoListResponse;

public class YoutubeConnector {
    YouTube service;

    private YouTube.Search.List query;
    private YouTube.PlaylistItems.List listPlaylistItems;
    private YouTube.Playlists.List listPlaylists;
    private YouTube.PlaylistItems.Delete delPlayListItems;

    private YouTube.Videos.List viewQuery;

    private static final String PLAYLIST = "SJSU-CMPE-277";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String KEY = "AIzaSyBCYmOv623nMOr-EwuGbUgg6dxr7NeF52c";

    final HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();

    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    GoogleAccountCredential credential;
    Activity mActivity;
    String token2;

    public YoutubeConnector(Activity activity){
        mActivity = activity;
        credential = GoogleAccountCredential.usingOAuth2(activity, Collections.singleton(YouTubeScopes.YOUTUBE + " " + YouTubeScopes.YOUTUBE_FORCE_SSL + " " + YouTubeScopes.YOUTUBE_READONLY + " " + YouTubeScopes.YOUTUBEPARTNER));
        SharedPreferences settings =activity.getSharedPreferences("user", Context.MODE_PRIVATE);
        activity.getPreferences(Context.MODE_PRIVATE);
        credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
// Tasks client
        service =new YouTube.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("MyYouTube").build();
        try{
            token2 = credential.getToken();
            query = service.search().list("id,snippet");
            query.setType("video");
            query.setOauthToken(token2);
            query.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url, snippet/publishedAt)");
            query.setMaxResults(10L);


            viewQuery = service.videos().list("statistics");
            viewQuery.setOauthToken(token2);
            viewQuery.setFields("items(statistics/viewCount)");



            listPlaylistItems = service.playlistItems().list("id,contentDetails,snippet");
            listPlaylistItems.setOauthToken(token2);
            listPlaylistItems.setFields("items(id,contentDetails/videoId,snippet/title,snippet/thumbnails/default/url,snippet/publishedAt)");
            listPlaylistItems.setMaxResults(10L);
            delPlayListItems = service.playlistItems().delete("id");
            delPlayListItems.setOauthToken(token2);

            listPlaylists = service.playlists().list("id,snippet");
            listPlaylists.setFields("items(id,snippet/title)");
            listPlaylists.setOauthToken(token2);
            listPlaylists.setMine(true);


        }catch(Exception e){
            Log.d("YC", "Could not initialize: "+e);
            e.printStackTrace();
        }
    }

    public List<VideoItem> getVideosFromPlayList() {
        try {
            String playListId = listPlaylistItems.getPlaylistId();
            PlaylistListResponse response = listPlaylists.execute();
            List<VideoItem> res = new ArrayList<VideoItem>();
            if(playListId == null) {
                List<Playlist> list = response.getItems();
                for(Playlist p : list) {
                    if(p.getSnippet().getTitle().equals(PLAYLIST)) {
                        playListId = p.getId();
                        listPlaylistItems.setPlaylistId(playListId);
                        break;
                    }
                }
                if(playListId == null) {
                    playListId = insertPlaylist();
                    listPlaylistItems.setPlaylistId(playListId);
                    return res;
                }
            }
            PlaylistItemListResponse response2 = listPlaylistItems.execute();
            List<PlaylistItem> results = response2.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(PlaylistItem result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                // item.setDate(result.getSnippet().getPublishedAt().toString());
                //item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getContentDetails().getVideoId());
                item.setPlayListId(result.getId());
                items.add(item);
            }
            return items;
        } catch (Exception e) {
            Log.d("YC", "Could not get from playlist: "+e );
            e.printStackTrace();
        }
        return null;
    }

    private String insertPlaylist() throws IOException {
        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(PLAYLIST);
        playlistSnippet.setDescription("SJSU 277 Playlist");
        PlaylistStatus playlistStatus = new PlaylistStatus();
        playlistStatus.setPrivacyStatus("private");

        Playlist youTubePlaylist = new Playlist();
        youTubePlaylist.setSnippet(playlistSnippet);
        youTubePlaylist.setStatus(playlistStatus);
        YouTube.Playlists.Insert playlistInsertCommand =
                service.playlists().insert("snippet,status", youTubePlaylist);
        Playlist playlistInserted = playlistInsertCommand.execute();
        return playlistInserted.getId();
    }

    public List<VideoItem> search(String keywords){

        query.setQ(keywords);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
//                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                YouTube.Videos.List list = service.videos().list("statistics");
                list.setKey(KEY);
                list.setId(result.getId().getVideoId());

                viewQuery.setId(result.getId().getVideoId());
                VideoListResponse v=viewQuery.execute();
//                Log.i("* view count " + v.getItems().size(), "" + v.getItems().get(0).getStatistics().getViewCount());

                //  Video v = list.execute().getItems().get(0);
                item.setViewCount(v.getItems().get(0).getStatistics().getViewCount().toString());
                item.setDate(result.getSnippet().getPublishedAt().toString());
                items.add(item);
            }
            return items;
        }catch(Exception e){
            Log.d("YC", "Could not search: "+e );
            e.printStackTrace();
            return null;
        }
    }

    public void deleteItemsFromPlayList(List<String> plIDs) {
        for (String plID:plIDs) {
            delPlayListItems.setId(plID);
            try {
                delPlayListItems.execute();
            } catch (IOException e) {
                Log.d("YC", "Could not delete: "+e );
                e.printStackTrace();
            }
        }
    }

    public void insertItemToPlayList(String videoId, String title) {
        try {

            String playListId = listPlaylistItems.getPlaylistId();
            PlaylistListResponse response = listPlaylists.execute();
            List<VideoItem> res = new ArrayList<VideoItem>();
            if (playListId == null) {
                List<Playlist> list = response.getItems();
                for (Playlist p : list) {
                    if (p.getSnippet().getTitle().equals(PLAYLIST)) {
                        playListId = p.getId();
                        listPlaylistItems.setPlaylistId(playListId);
                        break;
                    }
                }
                if (playListId == null) {
                    playListId = insertPlaylist();
                    listPlaylistItems.setPlaylistId(playListId);
                }
            }
            System.err.println("INSERT PL:" + playListId + " ID:" + videoId);
            // Define a resourceId that identifies the video being added to the
            // playlist.
            ResourceId resourceId = new ResourceId();
            resourceId.setKind("youtube#video");
            resourceId.setVideoId(videoId);

            // Set fields included in the playlistItem resource's "snippet" part.
            PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
            playlistItemSnippet.setTitle(title);
            playlistItemSnippet.setPlaylistId(playListId);
            playlistItemSnippet.setResourceId(resourceId);

            // Create the playlistItem resource and set its snippet to the
            // object created above.
            PlaylistItem playlistItem = new PlaylistItem();
            playlistItem.setSnippet(playlistItemSnippet);

            // Call the API to add the playlist item to the specified playlist.
            // In the API call, the first argument identifies the resource parts
            // that the API response should contain, and the second argument is
            // the playlist item being inserted.
            YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
                    service.playlistItems().insert("snippet,contentDetails", playlistItem);
            playlistItemsInsertCommand.setOauthToken(token2);
            PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

}