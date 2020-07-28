package com.example.fakestrgam;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Dialogcomment extends AppCompatDialogFragment {



    private String texthint;
    private ProgressDialog progressDialog;
    private EditText inputDlg;
    private int numconut=300;
    private String tag=null;
    private Dialog dialog;
    private DialogcommentListener listener;


    public Dialogcomment(){

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog=new Dialog(getActivity(),R.style.Theme_Design_BottomSheetDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        View contentview=View.inflate(getActivity(),R.layout.layout_dialog,null);
        dialog.setContentView(contentview);
        dialog.setCanceledOnTouchOutside(true);
        Window window=dialog.getWindow();
        WindowManager.LayoutParams lp=window.getAttributes();
        lp.gravity= Gravity.BOTTOM;
        lp.alpha=1;
        lp.dimAmount=0.5f;
        lp.width=WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        inputDlg=(EditText)contentview.findViewById(R.id.addcomment);
        inputDlg.setHint(texthint);
        final ImageView sendcomment=(ImageView)contentview.findViewById(R.id.sendcomment);

        inputDlg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length()>0){
                    sendcomment.setBackgroundColor(Color.parseColor("#009688"));
                }else{
                    sendcomment.setBackgroundColor(Color.parseColor("#E4E2E2"));
                }
            }
        });

        sendcomment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(inputDlg.getText().toString())) {
                    Toast.makeText(getActivity(),"The context is empty",Toast.LENGTH_LONG).show();
                    return;
                } else {
                    hideSoftkeyboard();
                    String addcomment=inputDlg.getText().toString();
                    listener.applyTexts(addcomment);
                    dialog.dismiss();
                }

            }
        });

        inputDlg.setFocusable(true);
        inputDlg.setFocusableInTouchMode(true);
        inputDlg.requestFocus();
        final Handler handler=new Handler();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideSoftkeyboard();
                    }
                },200);
            }
        });
        return dialog;

    }

    public void hideSoftkeyboard(){
        try {
            ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener=(DialogcommentListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+"must implement DialogcommentLister");
        }
    }

    public interface DialogcommentListener{
        void applyTexts(String addcomment);
    }
}
