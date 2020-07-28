package com.example.fakestrgam.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fakestrgam.Checkpost;
import com.example.fakestrgam.PictureComment;
import com.example.fakestrgam.R;
import com.example.fakestrgam.Userpicture;
import com.example.fakestrgam.Userprofile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context mContext;
    private List<PictureComment> mData;
    private FirebaseFirestore firebaseFirestore;
    private String tempusername;


    public CommentAdapter(Context context, List<PictureComment>  mData) {
        this.mContext = context;
        this.mData = mData;
    }


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View row= LayoutInflater.from(mContext).inflate(R.layout.row_comment,parent,false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {

        firebaseFirestore=FirebaseFirestore.getInstance();

        Picasso.get().load(mData.get(position).getCurrentusericonuri())
                .placeholder(R.drawable.light_grey).fit().centerCrop().into(holder.img_user);
        holder.tv_name.setText(mData.get(position).getCurrentusername());
        holder.tv_comment.setText(mData.get(position).getComment());


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class CommentViewHolder extends RecyclerView.ViewHolder{

        ImageView img_user;
        TextView tv_name,tv_comment;


        public CommentViewHolder(View itmView){
            super(itmView);
            img_user=itmView.findViewById(R.id.comment_user_img);
            tv_comment=itmView.findViewById(R.id.comment_content);
            tv_name=itmView.findViewById(R.id.comment_username);
        }
    }

}
