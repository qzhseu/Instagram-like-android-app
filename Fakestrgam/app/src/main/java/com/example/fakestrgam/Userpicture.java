package com.example.fakestrgam;

import java.net.URL;

public class Userpicture {
    public String timeStamp;
    public String url;
    public String caption;
    public String email;

    public Userpicture(){

    }

    public Userpicture(String timeStamp,String url){
        this.timeStamp=timeStamp;
        this.url=url;
        this.caption=caption;
        this.email=email;
    }

    public String getUserPicTimeStamp() {
        return timeStamp;
    }

    public void setUserPicTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserPicUrl() {
        return url;
    }

    public void setUsePicUrl(String url) {
        this.url = url;
    }

    public String getUserPicCaption() {
        return caption;
    }

    public void setUserPicCaption(String caption) {
        this.caption = caption;
    }

    public String getUserEmail() {
        return email;
    }

    public void setUserEmail(String email) {
        this.email = email;
    }


}
