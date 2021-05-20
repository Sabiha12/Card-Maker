package com.sabiha.cardmaker.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabiha.cardmaker.Interface.ItemClickListener;
import com.sabiha.cardmaker.R;

public class FeaturedCardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView tvProductName, tvProductPrice;
    public ImageView ivProductImage;
    public ItemClickListener itemClickListener;

    public FeaturedCardsViewHolder(@NonNull View itemView) {
        super(itemView);
        ivProductImage = itemView.findViewById(R.id.ivProductImage);
    }

    @Override
    public void onClick(View view) {

    }
}
