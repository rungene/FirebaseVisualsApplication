package com.rungenes.firebasevisualsapplication

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import com.rungenes.firebasevisualsapplication.R
import com.squareup.picasso.Picasso
import com.rungenes.firebasevisualsapplication.ViewHolder.ClickListener
import android.view.View.OnLongClickListener
import android.widget.ImageView

class ViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
    //setting details to recyclerview row
    fun setDetails(context: Context?, title: String?, image: String?, description: String?) {
        //views
        val textViewTitle: TextView = view.findViewById(R.id.textViewTitle)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
        val imageViewRow: ImageView = view.findViewById(R.id.imageViewRow)

        /*       ModelClass modelClass = new ModelClass();

        textViewTitle.setText(modelClass.getTitle());
        textViewDescription.setText(modelClass.getDescription());

        Picasso.get().load(modelClass.getImage()).into(imageViewRow);*/textViewTitle.text = title
        Picasso.get().load(image).into(imageViewRow)
        textViewDescription.text = description
    }

    //interface to send callbacks
    private var mClickListener: ClickListener? = null

    interface ClickListener {
        fun onItemClick(view: View?, position: Int)
        fun onItemLongClick(view: View?, position: Int)
    }

    fun setOnClickListener(clickListener: ClickListener?) {
        mClickListener = clickListener
    }

    init {

        //on item click
        itemView.setOnClickListener { view ->
            if (mClickListener != null) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mClickListener!!.onItemClick(view, position)
                }
            }
        }
        itemView.setOnLongClickListener { view ->
            if (mClickListener != null) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    mClickListener!!.onItemLongClick(view, position)
                }
            }
            true
        }
    }
}