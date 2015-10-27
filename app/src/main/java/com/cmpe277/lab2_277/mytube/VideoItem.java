package com.cmpe277.lab2_277.mytube;


public class VideoItem {
    private String title;
//    private String description;
    private String thumbnailURL;
    private String id;
    private String date;
    private String viewCount;
    private String playListId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        if(this.title!= null && this.title.length() > 50) {
            this.title = title.substring(0, 40);
        }
    }

//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//        if(this.description != null && this.description.length() > 50) {
//            this.description = description.substring(0, 50);
//        }
//    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;
    }

    public String getPlayListId() {return playListId;}

    public void setPlayListId(String playListId) {this.playListId = playListId;}

}