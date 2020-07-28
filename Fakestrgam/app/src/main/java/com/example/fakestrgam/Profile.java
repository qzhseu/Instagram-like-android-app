package com.example.fakestrgam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fakestrgam.adapter.MyAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.suke.widget.SwitchButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private de.hdodenhof.circleimageview.CircleImageView ivprofileimg;
    private TextView tvusername,tvuserbio;
    private ImageButton btLogout;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String Authuseremail;
    private FirebaseStorage firebaseStorage;
    private Uri photouri;
    private ProgressBar loadinfoprogressbar,uploadprocessbar;
    private StorageReference storageReference;
    private String timeStamp;
    private ProgressDialog progressDialog;
    private String profileimgpath=null;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private final TreeMap<String,Userpicture> Picinfo=new TreeMap<>(Collections.reverseOrder());
    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private List<Userpicture> mpic;
    private String currentPhotoPath="@drawable/profilepicture";
    private Toolbar toolbar;
    public static final String EXTRA_MESSAGE = "com.example.android.Profile.extra.MESSAGE";

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private ImageCompressTask imageCompressTask;
    private Uri compressedphotouri;
    private String compressedPhotoPath="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);




        toolbar=(Toolbar) findViewById(R.id.title_bar);
        toolbar.bringToFront();
        setSupportActionBar(toolbar);

        btLogout=(ImageButton) findViewById(R.id.btLogout);
        tvusername=(TextView)findViewById(R.id.tvusernameprofile);
        tvuserbio=(TextView)findViewById(R.id.tvuserbioprofile);
        ivprofileimg=(CircleImageView) findViewById(R.id.ivprofileimg);
        loadinfoprogressbar=(ProgressBar)findViewById(R.id.loadpicprocessbar);
        uploadprocessbar=(ProgressBar)findViewById(R.id.progress_bar);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();



        Authuseremail=GetAuthUserEmail();


        loadinfoprogressbar.bringToFront();
        uploadprocessbar.bringToFront();

        progressDialog = new ProgressDialog(this);

        recyclerView=(RecyclerView) findViewById(R.id.recyclerView);
        layoutManager=new GridLayoutManager(Profile.this,3);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);


        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("bundle");
        if(bundle==null){
            LoadProfileInfo(Authuseremail,null,null,null);
        }else{
            profileimgpath=bundle.getString("Path");
            String tempUsername=bundle.getString("Username");
            String tempBio=bundle.getString("Bio");
            LoadProfileInfo(Authuseremail,profileimgpath,tempUsername,tempBio);
        }


        phtotogallery();//display photo gallery

       //


    }

       private void phtotogallery(){
        mpic=new ArrayList<>();
        loadinfoprogressbar.setVisibility(View.VISIBLE);
        // get all photos' url
        firebaseFirestore.collection("users").document(Authuseremail).collection("photos").addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                        Toast.makeText(Profile.this, "No such Picture document", Toast.LENGTH_SHORT).show();
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

                MyAdapter myAdapter=new MyAdapter(Profile.this,mpic);
                recyclerView.setAdapter(myAdapter);
                loadinfoprogressbar.setVisibility(View.GONE);
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
        Toast.makeText(Profile.this, "Log out Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Profile.this,MainActivity.class));
    }

    private String GetAuthUserEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            String email=user.getEmail();
            return email;
        }else{
            Toast.makeText(Profile.this, "Auth Fail", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    private void LoadProfileInfo(String authuseremail, String profileimgpath, String Username, String Bio){
        progressDialog.setMessage("Loading");
        progressDialog.show();
        loadinfoprogressbar.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(profileimgpath)||TextUtils.isEmpty(Username)||TextUtils.isEmpty(Bio)) {

            StorageReference storageReference=firebaseStorage.getReference();
            storageReference.child(firebaseAuth.getUid()).child("Images/Profile Pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(ivprofileimg);
                  //  progressDialog.dismiss();
                }
            });

            firebaseFirestore.collection("users").document(authuseremail).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document=task.getResult();
                                if(document.exists()){
                                    Userprofile userProfil=document.toObject(Userprofile.class);
                                    tvusername.setText(userProfil.getUsername());
                                    tvuserbio.setText(userProfil.getUserbio());
                                    // LoadUserpicinfo();
                                    progressDialog.dismiss();
                                }else{
                                    Toast.makeText(Profile.this, "No such document", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(Profile.this, "Get Failed with"+task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else {

            Bitmap imageBitmap = BitmapFactory.decodeFile(profileimgpath);
            ivprofileimg.setImageBitmap(imageBitmap);
            tvusername.setText(Username);
            tvuserbio.setText(Bio);
            progressDialog.dismiss();
        }
    }

    //

    public void AddPicture(View view) {
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

            imageCompressTask = new ImageCompressTask(this, currentPhotoPath, iImageCompressTaskListener);
            mExecutorService.execute(imageCompressTask);
            //uploadpic();
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
            compressedphotouri = FileProvider.getUriForFile(Profile.this,
                    "com.example.android.fileprovider", file);

            Intent intent=new Intent(Profile.this,caption.class);
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




    public void gotoglobal(View view) {
        Intent intent=new Intent(Profile.this,Globalgallery.class);
        startActivity(intent);
    }
}
