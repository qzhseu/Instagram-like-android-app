package com.example.fakestrgam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Register extends AppCompatActivity {

    private EditText userName, userPassword, userConfpassword, userEmail, userBio;
    private Button btSignup;
    private ImageView  userpicture;
    private TextView infoconfpass,infopass;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private ImageView profilepic;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Uri photouri;
    private Uri compressedphotouri;
    private Bitmap imageBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Boolean ismatch=false;
    private static final String TAG = Register.class.getSimpleName();
    private Uri downloadUri;

    private ExecutorService mExecutorService = Executors.newFixedThreadPool(1);
    private ImageCompressTask imageCompressTask;


    public static final String EXTRA_MESSAGE = "com.example.android.Register.extra.MESSAGE";

    private String currentPhotoPath="@drawable/profilepicture";
    private String compressedPhotoPath="";

    private ProgressDialog progressDialog;
    private Toolbar toolbar;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toolbar=(Toolbar) findViewById(R.id.titlebarregister);
        toolbar.bringToFront();
        setSupportActionBar(toolbar);

        setupUIViews();
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
       // StorageReference myRefl=storageReference.child(firebaseAuth.getUid());

        progressDialog=new ProgressDialog(this);
        userConfpassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                infoconfpass.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

                String confirmpassword=userConfpassword.getText().toString();
                String password=userPassword.getText().toString();
                if(confirmpassword.equals(password)){
                    ismatch=true;
                    infoconfpass.setText("");
                }else {
                    ismatch=false;
                    infoconfpass.setText("Password do not match");


                }
            }
        });


    }


    private void setupUIViews(){
        userBio=(EditText)findViewById(R.id.etbio);
        userEmail=(EditText)findViewById(R.id.etregemail);
        userConfpassword=(EditText)findViewById(R.id.etregconfirmpassword);
        userPassword=(EditText)findViewById(R.id.etregpassword);
        userName=(EditText)findViewById(R.id.etusername);
        btSignup=(Button)findViewById(R.id.btregsignup);
        userpicture=(ImageView)findViewById(R.id.ivpicture);
        profilepic=(ImageView)findViewById(R.id.ivprofileimg) ;
        infoconfpass=(TextView)findViewById(R.id.tvinfoconfirpass);
        infopass=(TextView)findViewById(R.id.tvinfopassword);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.back:
                BackMain();
        }
        return super.onOptionsItemSelected(item);
    }

    private void BackMain() {
        startActivity(new Intent(Register.this,MainActivity.class));
    }

    // Users register with email and password
    public void SIGNUP(View view) {

        progressDialog.setMessage("Signing up");
        progressDialog.show();

        if(validate()&&ismatch){
            //Upload data to the database
            String user_email=userEmail.getText().toString().trim();
            String user_password=userPassword.getText().toString().trim();

            firebaseAuth.createUserWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()) {
                        progressDialog.dismiss();
                        uploadpic();
                        Intent intent=new Intent(Register.this,Profile.class);
                        Bundle bundle=new Bundle();
                        bundle.putString("Path", compressedPhotoPath);
                        bundle.putString("Username",userName.getText().toString());
                        bundle.putString("Bio",userBio.getText().toString());
                        intent.putExtra("bundle",bundle);
                        startActivity(intent);
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(Register.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            } );

        }
    }

    // Check information whether is completed
    private Boolean validate(){
        Boolean result=false;
        String username=userName.getText().toString();
        String password=userPassword.getText().toString();
        String email=userEmail.getText().toString();

        if(username.isEmpty() || password.isEmpty() || email.isEmpty())
        {
            Toast.makeText(this,"Please enter all the details",Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        }else{
            result=true;
        }
        return result;
    }


    private void uploadpic(){
        final StorageReference imageReference=storageReference.child(firebaseAuth.getUid()).child("Images").child("Profile Pic");
        UploadTask uploadTask=imageReference.putFile(compressedphotouri);

        Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return imageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    downloadUri=task.getResult();
                    sendUserData(downloadUri);
                }
            }
        });
    }

    //Store users' information to Firebase
    private void sendUserData(Uri uri){

        firebaseFirestore=FirebaseFirestore.getInstance();
        Map<String,Object> user =new HashMap<>();
        user.put("username",userName.getText().toString());
        user.put("bio",userBio.getText().toString());
        user.put("displaypic",currentPhotoPath);
        user.put("iconuri",compressedphotouri.toString());

        firebaseFirestore.collection("users").document(userEmail.getText().toString()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Toast.makeText(Register.this, "Registration Successful and Upload info Successful", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Register.this, "Registration Successful but Upload info Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // Take picture head shot
    public void TakePicture(View view) {

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



    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {//Refuse
                Toast.makeText(this, "Please grant read and write permissions, otherwise the application will be unusable!", Toast.LENGTH_LONG).show();
                Register.this.finish();
            }
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
                startActivityForResult(takePictureIntent,REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        Log.d("ImageCompressor", "Original photo size ==> " + image.length());
        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //Toast.makeText(Register.this,currentPhotoPath,Toast.LENGTH_LONG).show();
            imageBitmap = BitmapFactory.decodeFile(currentPhotoPath);
            //userpicture.setImageBitmap(imageBitmap);
            //Log.d("ImageCompressor", "Original photo path==> " + currentPhotoPath);

            imageCompressTask = new ImageCompressTask(this, currentPhotoPath, iImageCompressTaskListener);
            mExecutorService.execute(imageCompressTask);

            Log.d("onScanCompleted", "original photo Uri==> "+photouri.getPath());

            Log.d("onScanCompleted", "new photo path==> "+compressedPhotoPath);



        }
    }

    private IImageCompressTaskListener iImageCompressTaskListener = new IImageCompressTaskListener() {
        @Override
        public void onComplete(List<File> compressed) {
            //photo compressed. Yay!

            //prepare for uploads. Use an Http library like Retrofit, Volley or async-http-client (My favourite)

            File file = compressed.get(0);

           // Log.d("ImageCompressor", "New photo size ==> " + file.length()); //log new file size.
           // compressedphotouri = FileProvider.getUriForFile(Register.this,"com.example.android.fileprovider",file);
           // MediaScannerConnection.scanFile(this,new String[]{currentPhotoPath},null,(String path,Uri uri)->Log.i("ImageCompressor",uri.toString()));


            compressedphotouri=Uri.fromFile(new File(compressedPhotoPath));
            userpicture.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            compressedPhotoPath=file.getAbsolutePath();
            Log.d("ImageCompressor", "New photo path==> " + compressedphotouri.getPath()); //log new file size.
            compressedphotouri = FileProvider.getUriForFile(Register.this,
                    "com.example.android.fileprovider", file);

           /* MediaScannerConnection.scanFile(Register.this,
                    new String[] { file.getAbsolutePath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.d("onScanCompleted", "New photo Uri==> "+uri.getPath());
                        }
                    });*/
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




    @Override
    public boolean dispatchTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, me)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(me);
    }

    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {

                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


}


