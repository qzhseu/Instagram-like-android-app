package com.example.fakestrgam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ZoominPic extends AppCompatActivity {

    private ImageView picture;
    private String pictureuri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoomin_pic);

        picture=(ImageView)findViewById(R.id.zoompicture);
        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("bundle");
        pictureuri=bundle.getString("PictureUri");
        Picasso.get().load(pictureuri).into(picture);

    }

    public void goback(View view) {

        Intent intent=new Intent(this,Checkpost.class);
        startActivity(intent);
    }
}
