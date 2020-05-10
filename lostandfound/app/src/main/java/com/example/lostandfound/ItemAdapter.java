package com.example.lostandfound;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ProductViewHolder> {


    private Context mCtx;
    private List<Item> productList;

    //progress bar
    private ProgressDialog progressDialog;

    public ItemAdapter(Context mCtx, List<Item> productList) {
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
        holder.textViewDesc.setText(product.getDescription());
        holder.textViewDate.setText(String.valueOf(product.getDate()));


            if(!product.getImage().contains("null")){
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
        }else{

            //default image
            holder.imageView.setImageResource(R.drawable.lostfoundicon);


        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), Viewing.class);
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

        TextView textViewTitle, textViewDesc, textViewDate, textViewPrice;
        ImageView imageView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.post_title);
            textViewDesc = itemView.findViewById(R.id.post_desc);
            textViewDate = itemView.findViewById(R.id.post_date);
            imageView = itemView.findViewById(R.id.post_image);
        }
    }
}