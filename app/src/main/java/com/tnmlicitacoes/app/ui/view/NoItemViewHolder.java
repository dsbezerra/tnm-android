package com.tnmlicitacoes.app.ui.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;

public class NoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView noItemTv;

    private OnClickListenerRecyclerView itemClickListener;

    public NoItemViewHolder(View itemView, OnClickListenerRecyclerView listenerHack) {
        super(itemView);
        this.noItemTv = (TextView) itemView.findViewById(R.id.noItemText);
        this.itemView.setClickable(true);
        this.itemView.setOnClickListener(this);
        this.itemClickListener = listenerHack;
    }

    @Override
    public void onClick(View v) {
        if(itemClickListener != null)
            v.requestFocus();
        itemClickListener.OnClickListener(v, getPosition());
    }
}

