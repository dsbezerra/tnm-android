package com.tnmlicitacoes.app.ui.adapter.holder;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;


public class SimpleListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private OnClickListenerRecyclerView mClickListener;

    public TextView text1;

    public SimpleListItemViewHolder(View itemView) {
        super(itemView);
        this.text1 = (TextView) itemView.findViewById(android.R.id.text1);
        itemView.setOnClickListener(this);
        itemView.setClickable(true);

        Context context = itemView.getContext();
        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray typedArray = context.obtainStyledAttributes(attrs);
        int backgroundResource = typedArray.getResourceId(0, 0);
        itemView.setBackgroundResource(backgroundResource);
        typedArray.recycle();
    }

    public void setOnClickListener(OnClickListenerRecyclerView listener) {
        this.mClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mClickListener != null) {
            mClickListener.OnClickListener(v, getAdapterPosition());
        }
    }
}
