package com.example.fakestrgam;

import java.net.URL;

public class Userprofile {
    public String bio;
    public String username;
    public String url;
    public String iconuri;


    public Userprofile(){

    }

    public Userprofile(String userbio,String userurl,String username){
        this.bio=userbio;
        this.url=userurl;
        this.username=username;
        this.iconuri=iconuri;
    }

    public String getUserbio() {
        return bio;
    }

    public void setUserbio(String userbio) {
        this.bio = userbio;
    }

    public String getUserurl() {
        return url;
    }

    public void setUserurl(String userurl) {
        this.url = userurl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsericonuri() {
        return iconuri;
    }

    public void setUsericonuri(String iconuri) {
        this.iconuri = iconuri;
    }
}
