package com.example.fakestrgam;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.fakestrgam.adapter.GlobalAdapter;
import com.example.fakestrgam.adapter.MyAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.suke.widget.SwitchButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Globalgallery extends AppCompatActivity {


    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private String Authuseremail;
    private Uri photouri;
    private String timeStamp;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath="@drawable/profilepicture";
    public static final String EXTRA_MESSAGE = "com.example.android.Globalgallery.extra.MESSAGE";
    private final TreeMap<String,Userpicture> Picinfo=new TreeMap<>(Collections.reverseOrder());
    private List<Userpicture> mpic;
    private ProgressBar processbar;

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private ImageCompressTask imageCompressTask;
    private Uri compressedphotouri;
    private String compressedPhotoPath="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_globalgallery);

        Toolbar toolbar=(Toolbar)findViewById(R.id.titlebar);
        toolbar.bringToFront();
        setSupportActionBar(toolbar);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
        Authuseremail=GetAuthUserEmail();

        recyclerView=(RecyclerView) findViewById(R.id.recyclerView);
        layoutManager=new GridLayoutManager(Globalgallery.this,1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        processbar=(ProgressBar)findViewById(R.id.loadpicprocessbarglobal);

        processbar.bringToFront();

        phtotogallery();

    }

    private void phtotogallery(){
        mpic=new ArrayList<>();
        processbar.setVisibility(View.VISIBLE);
        // get all photos' url
        firebaseFirestore.collection("globalphotos").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                    mpic.clear();
                    if(document.exists()){
                        Userpicture userpicture=document.toObject(Userpicture.class);
                        //firstly store userpicture to Treemap<string,userpicture> to list photos in chronological order
                        if(Picinfo.containsKey(userpicture.timeStamp)){
                            //
                        }else{
                            Picinfo.put(userpicture.timeStamp,userpicture);
                        }
                    }else {
                        Toast.makeText(Globalgallery.this, "No such Picture document", Toast.LENGTH_SHORT).show();
                    }
                }
                // Store userpicture into List<> from Treemap<string,userpicture>
                Iterator<Map.Entry<String,Userpicture>> io=Picinfo.entrySet().iterator();
                while (io.hasNext()){
                    Map.Entry<String,Userpicture> me=io.next();
                    final String key=(String)me.getKey();
                    final Userpicture value=me.getValue();
                    mpic.add(value);
                }

                GlobalAdapter myAdapter=new GlobalAdapter(Globalgallery.this,mpic);
                recyclerView.setAdapter(myAdapter);
                processbar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbaricon,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                LOGOUT();
        }
        return super.onOptionsItemSelected(item);
    }

    private void LOGOUT() {
        firebaseAuth.signOut();
        Toast.makeText(Globalgallery.this, "Log out Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Globalgallery.this,MainActivity.class));
    }


    private String GetAuthUserEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            String email=user.getEmail();
            return email;
        }else{
            Toast.makeText(Globalgallery.this, "Auth Fail", Toast.LENGTH_SHORT).show();
            return null;
        }

    }
    public void gotoprofile(View view) {
        Intent intent=new Intent(Globalgallery.this,Profile.class);
        startActivity(intent);
    }

    public void takepicture(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android 6.0以上
            int writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }else{
                dispatchTakePictureIntent();
            }
        }else{//android 6.0以下
            dispatchTakePictureIntent();

        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File imgFile = null;
            try {
                imgFile = createImageFile();
            } catch (IOException e) {
                // Error occurred while creating the File
                e.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (imgFile != null) {
                photouri = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        imgFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photouri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Bitmap imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            imageCompressTask = new ImageCompressTask(this, currentPhotoPath, iImageCompressTaskListener);
            mExecutorService.execute(imageCompressTask);
        }
    }

    private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
        @Override
        public void onComplete(List<File> compressed) {
            //photo compressed. Yay!

            //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)

            File file = compressed.get(0);

            compressedphotouri=Uri.fromFile(new File(compressedPhotoPath));
            compressedPhotoPath=file.getAbsolutePath();
            Log.d("ImageCompressor", "New photo path==> " + compressedphotouri.getPath()); //log new file size.
            compressedphotouri = FileProvider.getUriForFile(Globalgallery.this,
                    "com.example.android.fileprovider", file);

            Intent intent=new Intent(Globalgallery.this,caption.class);
            Bundle bundle=new Bundle();
            bundle.putString("Path", compressedPhotoPath);
            bundle.putString("Uri",compressedphotouri.toString());
            bundle.putString("TimeStamp",timeStamp);
            bundle.putString("Authemail",Authuseremail);
            intent.putExtra("bun",bundle);
            startActivity(intent);

        }

        @Override
        public void onError(Throwable error) {
            //very unlikely, but it might happen on a device with extremely low storage.
            //log it, log.WhatTheFuck?, or show a dialog asking the user to delete some files....etc, etc
            Log.wtf("ImageCompressor", "Error occurred", error);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //clean up!
        mExecutorService.shutdown();

        mExecutorService = null;
        imageCompressTask = null;
    }




}
