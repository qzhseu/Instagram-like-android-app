package com.example.fakestrgam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class caption extends AppCompatActivity {

    private ImageView picture;
    private EditText caption;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private Uri photouri;
    private String timeStamp;
    private String Authuseremail;
    private ProgressBar uploadprocessbar;
    private Bitmap imageBitmap;
    private String LabelString;

    private String tempString;

    private ArrayList<String> mllabel;

    private FirebaseVisionImage mlimage;
    FirebaseVisionImageLabeler labeler;


    private com.suke.widget.SwitchButton switchbutton;

    private String imgpath;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();

        uploadprocessbar=(ProgressBar)findViewById(R.id.caption_progress_bar);

        picture=(ImageView)findViewById(R.id.picture);
        caption=(EditText)findViewById(R.id.caption);
        switchbutton=(com.suke.widget.SwitchButton)findViewById(R.id.switch_ML);
        imgpath=null;

        String uid=firebaseAuth.getUid().toString();

        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("bun");
        Authuseremail=bundle.getString("Authemail");
        timeStamp=bundle.getString("TimeStamp");
        imgpath=bundle.getString("Path");
        String uri=bundle.getString("Uri");

        photouri=Uri.parse(uri);
        //imgpath=intent.getStringExtra(Profile.EXTRA_MESSAGE);

        //caption.setText(imgpath);
        imageBitmap = BitmapFactory.decodeFile(imgpath);
        picture.setImageBitmap(imageBitmap);



        mllabel=new ArrayList<>();
        MLaddimagelabel(imageBitmap);

        switchbutton=(SwitchButton)findViewById((R.id.switch_ML));
        switchbutton.toggle();
        switchbutton.toggle(true);
        switchbutton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if(isChecked){
                    tempString=caption.getText().toString();
                    caption.setText(tempString+LabelString);
                   // Toast.makeText(caption.this,"On" , Toast.LENGTH_SHORT).show();
                }else{
                    //caption.setText("");
                    caption.setText(tempString);
                  //  Toast.makeText(caption.this,"off" , Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void MLaddimagelabel(Bitmap bitmap) {

        LabelString=new String();
        FirebaseVisionOnDeviceImageLabelerOptions options = new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build();
        labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler(options);
        mlimage= FirebaseVisionImage.fromBitmap(bitmap);
        labeler.processImage(mlimage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                for(FirebaseVisionImageLabel label:firebaseVisionImageLabels){
                    float confidence=label.getConfidence();
                    String text=label.getText();
                   // Toast.makeText(caption.this, text, Toast.LENGTH_SHORT).show();
                    LabelString=LabelString+"#"+text+" ";
                }
                //Toast.makeText(caption.this,LabelString, Toast.LENGTH_SHORT).show();
                //Toast.makeText(caption.this,String.valueOf(mllabel.size()), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("ML","Fail adding labels", e);
            }
        });

    }

    public void PostPicture(View view) {
        uploadpic();
    }

    public void Cancel(View view) {
        caption=null;
        picture=null;
        Intent intent=new Intent(caption.this,Profile.class);
        startActivity(intent);
    }

    private void uploadpic(){
        final StorageReference imageReference=storageReference.child(firebaseAuth.getUid()).child("Images").child("Fakestrgam Pic").child(timeStamp);

        uploadprocessbar.setVisibility(View.VISIBLE);
        UploadTask uploadTask=imageReference.putFile(photouri);
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
                    Uri downloadUri=task.getResult();
                    Storepicurl(downloadUri);
                }
            }
        });
    }

    private void Storepicurl(Uri uri){
        String usercaption=caption.getText().toString().trim();
        if(usercaption.isEmpty()){
            usercaption="0";
        }

        String photoid=Authuseremail+'+'+timeStamp;

        final Map<String,Object> photo =new HashMap<>();
        photo.put("email",Authuseremail);
        photo.put("url",uri.toString());
        photo.put("timeStamp",timeStamp);
        photo.put("caption",usercaption);
       // photo.put("photoid",photoid);

        firebaseFirestore.collection("globalphotos").document(photoid).set(photo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

        firebaseFirestore.collection("users").document(Authuseremail).collection("photos").document(timeStamp).set(photo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        uploadprocessbar.setVisibility(View.GONE);
                        Intent intent=new Intent(caption.this,Profile.class);
                        Toast.makeText(caption.this, "Upload picture Successful", Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

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
