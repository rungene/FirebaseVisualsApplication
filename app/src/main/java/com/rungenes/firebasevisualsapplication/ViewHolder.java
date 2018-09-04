package com.rungenes.firebasevisualsapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ViewHolder extends RecyclerView.ViewHolder {


    View view;

    public ViewHolder(@NonNull View itemView) {

        super(itemView);
        view = itemView;

        //on item click

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mClickListener!=null) {

                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {

                        mClickListener.onItemClick(view, position);
                    }
                }

            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {



                return true;
            }
        });
    }

    //setting details to recyclerview row

    public void setDetails(Context context, String title, String image, String description) {
        //views
        TextView textViewTitle, textViewDescription;
        ImageView imageViewRow;

        textViewTitle = view.findViewById(R.id.textViewTitle);
        textViewDescription = view.findViewById(R.id.textViewDescription);
        imageViewRow = view.findViewById(R.id.imageViewRow);

 /*       ModelClass modelClass = new ModelClass();

        textViewTitle.setText(modelClass.getTitle());
        textViewDescription.setText(modelClass.getDescription());

        Picasso.get().load(modelClass.getImage()).into(imageViewRow);*/

        textViewTitle.setText(title);
        Picasso.get().load(image).into(imageViewRow);
        textViewDescription.setText(description);

    }

    //interface to send callbacks

    private ViewHolder.ClickListener mClickListener;

    public interface ClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);

    }

    public void setOnClickListener(ViewHolder.ClickListener clickListener){
        mClickListener = clickListener;
    }


}
