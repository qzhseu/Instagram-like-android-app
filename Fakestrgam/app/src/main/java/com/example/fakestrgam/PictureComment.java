package com.example.fakestrgam;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class PictureComment {
    public String userComment;
    public String commentTimestamp;
    private String currentusername;
    private String currentusericonuri;

    public PictureComment(String comment, String commentTimestamp,String currentusericonuri,String currentusername) {
        this.userComment = comment;
        this.commentTimestamp = commentTimestamp;
        this.currentusericonuri=currentusericonuri;
        this.currentusername=currentusername;
    }

    public PictureComment(){

    }

    public String getComment() {
        return userComment;
    }
    public String getCurrentusername() {
        return currentusername;
    }
    public String getCurrentusericonuri() {
        return currentusericonuri;
    }

    public String getCommentTimestamp() {
        return commentTimestamp;
    }


    public void setComment(String comment) {
        this.userComment = comment;
    }


    public void setCommentTimestamp(String commentTimestamp) {
        this.commentTimestamp = commentTimestamp;
    }

    public void setCurrentusername(String currentusername) {
        this.currentusername = currentusername;
    }

    public void setCurrentusericonuri(String currentusericonuri) {
        this.currentusericonuri = currentusericonuri;
    }
}
