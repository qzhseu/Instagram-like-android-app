package com.example.fakestrgam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText Email;
    private EditText Password;
    private TextView Info_Email;
    private TextView Info_Password;
    private Button login;
    private Button signup;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=(Toolbar) findViewById(R.id.titlebarmain);
        toolbar.bringToFront();
        setSupportActionBar(toolbar);




        Email=(EditText)findViewById(R.id.etregemail);
        Password=(EditText)findViewById(R.id.etregpassword);
        Info_Email=(TextView)findViewById(R.id.tvinfoemail);
        Info_Password=(TextView)findViewById(R.id.tvinfopassword);
        login=(Button)findViewById(R.id.btLogin);
        signup=(Button)findViewById(R.id.btSignup);

        firebaseAuth=FirebaseAuth.getInstance();
        FirebaseUser user=firebaseAuth.getCurrentUser();

        progressDialog = new ProgressDialog(this);

        if(user!=null)
        {
            finish();
            startActivity(new Intent(MainActivity.this,Profile.class));
        }

        Email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Info_Email.setText("");

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                Info_Password.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void Registration(View view) {
        Intent intent=new Intent(this,Register.class);
        startActivity(intent);
    }

    public void Login(View view) {

        if(Email.getText().toString().isEmpty()) {
            Toast.makeText(this,"Please enter your e-mail",Toast.LENGTH_SHORT).show();
        }else if(Password.getText().toString().isEmpty()){
            Toast.makeText(this,"Please enter your password",Toast.LENGTH_SHORT).show();
        }else{

            verify(Email.getText().toString(), Password.getText().toString());

        }
        //Toast.makeText(MainActivity.this,"finish",Toast.LENGTH_LONG).show();
    }

    private void verify(String userEmail, String userPassword){
        progressDialog.setMessage("Verifying");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    startActivity(new Intent(MainActivity.this,Profile.class));
                }else{
                    String errorCode = ((FirebaseAuthException)task.getException()).getErrorCode();
                    switch (errorCode) {

                        case "ERROR_INVALID_EMAIL":
                            progressDialog.dismiss();
                            Info_Email.setText("The email address is badly formatted");
                            break;

                        case "ERROR_WRONG_PASSWORD":
                            progressDialog.dismiss();
                            Info_Password.setText("The password is invalid");
                            break;

                        case "ERROR_USER_NOT_FOUND":
                            progressDialog.dismiss();
                            Info_Email.setText("The email address is invalid");
                            break;

                    }
                }
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
