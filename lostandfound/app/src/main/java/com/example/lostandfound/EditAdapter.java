package com.example.lostandfound;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class EditAdapter extends RecyclerView.Adapter<EditAdapter.ProductViewHolder> {


    private Context mCtx;
    private List<Item> productList;

    public EditAdapter(Context mCtx, List<Item> productList) {
        this.mCtx = mCtx;
        this.productList = productList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.item_row, parent, false);

        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, int position) {
        final Item product = productList.get(position);

        holder.textViewTitle.setText(product.getName());
        holder.textViewDescrip.setText(product.getDescription());
        holder.textViewDate.setText(String.valueOf(product.getDate()));

        String loc = "uploads/" + product.getImage();

        StorageReference storageRef =
                FirebaseStorage.getInstance().getReference();
        storageRef.child(loc.trim()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(mCtx)
                        .load(uri)
                        .into(holder.imageView);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                // progressDialog.dismiss();

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), Entry.class);

                Bundle b = new Bundle();
                b.putBoolean("edit", true);
                intent.putExtras(b);

                intent.putExtra("name", product.getName());
                intent.putExtra("description", product.getDescription());
                intent.putExtra("date", product.getDate());
                intent.putExtra("location", product.getLocation());
                intent.putExtra("image", product.getImage());
                intent.putExtra("ctime", product.getCtime());
                intent.putExtra("username", product.getUsername());
                intent.putExtra("id", product.getId());

                v.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle, textViewDescrip, textViewDate;
        ImageView imageView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.post_title);
            textViewDescrip = itemView.findViewById(R.id.post_desc);
            textViewDate = itemView.findViewById(R.id.post_date);
            imageView = itemView.findViewById(R.id.post_image);
        }
    }
}