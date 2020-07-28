package com.example.fakestrgam.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fakestrgam.Checkpost;
import com.example.fakestrgam.Userpicture;
import com.squareup.picasso.Picasso;

import com.example.fakestrgam.R;

import java.util.List;

public class GlobalAdapter extends RecyclerView.Adapter<GlobalAdapter.MyHolder> {


    private Context context;
    // List<Drawable> IconId=new ArrayList<Drawable>();
    private List<Userpicture> images;

    public GlobalAdapter(Context context, List<Userpicture>  images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View layout= LayoutInflater.from(parent.getContext()).inflate(R.layout.global_item_layout,null);
        MyHolder myHolder=new MyHolder(layout);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        holder.setData(position);
        Userpicture uploadCurrent=images.get(position);
        Picasso.get().load(uploadCurrent.getUserPicUrl())
                .placeholder(R.drawable.light_grey)
                .fit().centerCrop().into(holder.img);

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    View.OnClickListener onClickListener=new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            int position=(int)view.getTag();
            String flag="2";
            String email=images.get(position).getUserEmail();
            String caption=images.get(position).getUserPicCaption();
            String url=images.get(position).getUserPicUrl();
            String timestamp=images.get(position).getUserPicTimeStamp();
            // ImageView imageView=getView(position);

            Intent intent=new Intent(context, Checkpost.class);
            Bundle bundle=new Bundle();
            bundle.putString("Useremail", email);
            bundle.putString("PictureUri",url);
            bundle.putString("TimeStamp",timestamp);
            bundle.putString("Caption",caption);
            bundle.putString("flag",flag);
            intent.putExtra("bundle",bundle);
            view.getContext().startActivity(intent);
        }
    };

    private ImageView getView(int position){
        ImageView imageView=new ImageView(context);
        imageView.setLayoutParams(new ViewGroup
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Picasso.get().load(images.get(position).getUserPicUrl()).into(imageView);
        return imageView;
    }

    public class MyHolder extends RecyclerView.ViewHolder{
        ImageView img;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(onClickListener);
            img=itemView.findViewById(R.id.global_item_image);

        }

        public void setData(int position){
            itemView.setTag(position);
        }


    }


}