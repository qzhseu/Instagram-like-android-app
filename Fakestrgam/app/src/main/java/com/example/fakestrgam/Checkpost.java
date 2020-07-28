package com.example.fakestrgam;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fakestrgam.adapter.CommentAdapter;
import com.example.fakestrgam.adapter.GlobalAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Checkpost extends AppCompatActivity implements Dialogcomment.DialogcommentListener {

    private Toolbar toolbar;

    private ImageView ivusericon,ivpicture;
    private TextView tvusername,tvcaption;
    private String caption, email,pictureuri,timestamp,flag;
    private String CurrentAuthuseremail,currentusername,currenticonurl,documentid;
    private FloatingActionButton addcomment;

    private String comment;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    private ProgressBar loadinfoprogressbar,uploadprocessbar;
    private ProgressDialog progressDialog;

    private ArrayList<String> commentuid;
    private List<PictureComment> mcomment;
    private final TreeMap<String,PictureComment> Commentinfo=new TreeMap<>();

    RecyclerView rvComment;
    CommentAdapter commentAdapter;

    Dialog dialogpicture;

    private ImageView zoompicture;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkpost);

        rvComment=findViewById(R.id.rv_comment);
        rvComment.setLayoutManager(new LinearLayoutManager(this));

        addcomment=(FloatingActionButton)findViewById(R.id.addcomment);
        loadinfoprogressbar=(ProgressBar)findViewById(R.id.progress_bar);
        progressDialog = new ProgressDialog(this);

        ivusericon=(ImageView)findViewById(R.id.ivicon);
        ivpicture=(ImageView)findViewById(R.id.ivpicture);
        tvusername=(TextView)findViewById(R.id.tvusername);
        tvcaption=(TextView)findViewById(R.id.tvcaption);

        firebaseAuth=FirebaseAuth.getInstance();
        CurrentAuthuseremail=GetAuthUserEmail();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();


        Intent intent=getIntent();
        Bundle bundle=intent.getBundleExtra("bundle");
        caption=bundle.getString("Caption");
        email=bundle.getString("Useremail");
        pictureuri=bundle.getString("PictureUri");
        timestamp=bundle.getString("TimeStamp");
        flag=bundle.getString("flag");
        documentid=email+'+'+timestamp;

        commentuid=new ArrayList<>();

        toolbar=(Toolbar) findViewById(R.id.titlebarcheckpost);
        toolbar.bringToFront();
        setSupportActionBar(toolbar);

        Loadpostinfo(email,pictureuri,timestamp,caption);
        ShowComment();

        dialogpicture=new Dialog(Checkpost.this,R.style.Theme_AppCompat_DayNight_DialogWhenLarge);
        dialogpicture.setContentView(R.layout.activity_zoomin_pic);

        zoompicture=(ImageView) dialogpicture.findViewById(R.id.zoompicture);
        Picasso.get().load(pictureuri).into(zoompicture);
        dialogpicture.setCanceledOnTouchOutside(true);


    }

    private void ShowComment() {
        mcomment=new ArrayList<>();
       // loadinfoprogressbar.setVisibility(View.VISIBLE);

        firebaseFirestore.collection("comments").document(documentid).collection("PictureComments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                    mcomment.clear();
                    if(document.exists()){
                        PictureComment pictureComment=document.toObject(PictureComment.class);
                        if(Commentinfo.containsKey(pictureComment.commentTimestamp)){

                        }else {
                            Commentinfo.put(pictureComment.commentTimestamp,pictureComment);
                        }

                    }
                }
                Iterator<Map.Entry<String,PictureComment>> io=Commentinfo.entrySet().iterator();
                while (io.hasNext()){
                    Map.Entry<String,PictureComment> me=io.next();
                    final String key=(String)me.getKey();
                    final PictureComment value=me.getValue();
                    mcomment.add(value);
                    //Log.d("Comment",value.commentTimestamp);
                }
                commentAdapter=new CommentAdapter(Checkpost.this,mcomment);
                rvComment.setAdapter(commentAdapter);
           //     loadinfoprogressbar.setVisibility(View.GONE);

            }
        });

    }

    private String GetAuthUserEmail(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            String email=user.getEmail();
            return email;
        }else{
            Toast.makeText(Checkpost.this, "Auth Fail", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void Loadpostinfo(String email, final String pictureuri, String timestamp, final String caption){
        progressDialog.setMessage("Loading");
        progressDialog.show();

        firebaseFirestore.collection("users").document(email)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    if(document.exists()){
                        Userprofile userProfil=document.toObject(Userprofile.class);
                        final String username=userProfil.getUsername();
                        final String iconuri=userProfil.getUsericonuri();
                        tvusername.setText(username);
                        tvcaption.setText(caption);
                        Picasso.get().load(pictureuri).into(ivpicture);
                        Picasso.get().load(iconuri).fit().centerCrop().into(ivusericon);
                        progressDialog.dismiss();
                    }else{
                        Toast.makeText(Checkpost.this, "No such document", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(Checkpost.this, "Get Failed with"+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(CurrentAuthuseremail.equals(email)){
            getMenuInflater().inflate(R.menu.delete,menu);
        }else{
            getMenuInflater().inflate(R.menu.back,menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                Deletepost();
            case R.id.back:
                Back();
        }
        return super.onOptionsItemSelected(item);
    }

    private void Deletepost() {

        deleteusers();
        deleteglobal();
        deletestorage();
        deletecomments();
    }

    private void deletecomments() {
        commentuid.clear();
        firebaseFirestore.collection("comments").document(documentid).collection("PictureComments")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for(QueryDocumentSnapshot document:queryDocumentSnapshots){
                    commentuid.add(document.getId());

                }
                final Iterator it1 = commentuid.iterator();
                while(it1.hasNext()){
                   firebaseFirestore.collection("comments").document(documentid)
                           .collection("PictureComments").document(it1.next().toString())
                           .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                         //  Toast.makeText(Checkpost.this,it1.next().toString()+"Deleted success", Toast.LENGTH_SHORT).show();
                       }
                   }).addOnFailureListener(new OnFailureListener() {
                       @Override
                       public void onFailure(@NonNull Exception e) {
                         //  Toast.makeText(Checkpost.this,it1.next().toString()+"Deleted fail", Toast.LENGTH_SHORT).show();
                       }
                   });
                }
                firebaseFirestore.collection("comments").document(documentid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(Checkpost.this,"终于他妈的删干净了", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void deletestorage() {
        final StorageReference imageReference=storageReference.child(firebaseAuth.getUid()).child("Images").child("Fakestrgam Pic").child(timestamp);
        imageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Delete Document", "Picture successfully deleted!(Storage)");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Delete Document", "Error deleting document(Storage)", e);
            }
        });
        firebaseFirestore.collection("comments").document(documentid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Delete Document", "Comments successfully deleted!(comments)");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Delete Comments", "Error deleting comments(comments)", e);
            }
        });

    }

    private void deleteglobal() {
        firebaseFirestore.collection("globalphotos").document(documentid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Delete Document", documentid+"DocumentSnapshot successfully deleted!(global)");
                if(flag.equals("2")){
                    Intent intent=new Intent(Checkpost.this,Globalgallery.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Delete Document", "Error deleting document(global)", e);
            }
        });

    }

    private void deleteusers() {
        firebaseFirestore.collection("users").document(CurrentAuthuseremail)
                .collection("photos").document(timestamp).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Delete Document", "DocumentSnapshot successfully deleted!(user)");
                if(flag.equals("1")){
                    Intent intent=new Intent(Checkpost.this,Profile.class);
                    startActivity(intent);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w("Delete Document", "Error deleting document(users)", e);
            }
        });
    }

    private void Back() {
        if(flag.equals("1")){
            Intent intent=new Intent(Checkpost.this,Profile.class);
            startActivity(intent);
        }else {
            Intent intent=new Intent(Checkpost.this,Globalgallery.class);
            startActivity(intent);
        }
    }

    public void Addcomment(View view) {
        Dialogcomment dialogcomment=new Dialogcomment();
        dialogcomment.show(getSupportFragmentManager(),"Addcomment");
    }

    @Override
    public void applyTexts(String addcomment) {

        comment=addcomment;
        UploadComment(addcomment);
        // Toast.makeText(Checkpost.this,comment,Toast.LENGTH_LONG).show();
    }

    private void UploadComment(final String addcomment) {
        final String CommentTimestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        firebaseFirestore.collection("users").document(CurrentAuthuseremail)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document=task.getResult();
                    if(document.exists()){
                        Userprofile userProfil=document.toObject(Userprofile.class);
                        currentusername=userProfil.getUsername();
                        currenticonurl=userProfil.getUsericonuri();
                        //String documentid=email+"+"+timestamp;

                        PictureComment pictureComment=new PictureComment(addcomment,CommentTimestamp,currenticonurl,currentusername);
                       // firebaseFirestore.collection("globalphotos").document(documentid).collection("PictureComments").add(pictureComment)
                        firebaseFirestore.collection("comments").document(documentid).collection("PictureComments").add(pictureComment)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                       // Toast.makeText(Checkpost.this,"Send Successful",Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Checkpost.this,"Send Failed",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    public void ZoomPicture(View view) {

        dialogpicture.show();
        zoompicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogpicture.dismiss();
            }
        });
       /* Intent intent=new Intent(this,ZoominPic.class);
        Bundle bundle=new Bundle();
        bundle.putString("PictureUri",pictureuri);
        intent.putExtra("bundle",bundle);
        startActivity(intent);*/

    }
}
