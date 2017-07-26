package com.tnmlicitacoes.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.realm.Notice;

import java.util.ArrayList;
import java.util.List;

public class MyNoticesAdapter extends RecyclerView.Adapter<MyNoticesAdapter.VH> {

    private static final String TAG = "MyNoticesAdapter";

    private final Context mContext;

    private List<Notice> mPersistedNoticesList = new ArrayList<>();

    private OnClickListenerRecyclerView mRecViewOnClickListener;

    private static String sIconUrl = "http://tnmlicitacoes.com/img/app/categories/{categoryId}/icon.png";

    public MyNoticesAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mPersistedNoticesList.size();
    }

    public void setItems(List<Notice> list) {
        notifyDataSetChanged();
    }

    public void setOnClickListener (OnClickListenerRecyclerView listener) {
        this.mRecViewOnClickListener = listener;
    }

    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        public View itemView;
        public ImageView exclusiveMpe;
        public ImageView noticeInfoCategory;
        public TextView noticeInfoLocation;
        public TextView noticeModality;
        public TextView noticeNumber;
        public TextView noticeDescription;
        public TextView noticeInfoNewDate;
        public TextView noticeInfoRectified;
        public TextView noticeInfoOpenDate;

        public VH(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.exclusiveMpe = (ImageView) itemView.findViewById(R.id.exclusiveMpe);
            this.noticeInfoCategory = (ImageView) itemView.findViewById(R.id.noticeInfoCategory);
            this.noticeInfoLocation = (TextView) itemView.findViewById(R.id.agencyName);
            this.noticeModality = (TextView) itemView.findViewById(R.id.noticeModality);
            this.noticeNumber = (TextView) itemView.findViewById(R.id.noticeNumber);
            this.noticeDescription = (TextView) itemView.findViewById(R.id.noticeDescription);
            this.noticeInfoOpenDate = (TextView) itemView.findViewById(R.id.noticeOpenDate);
            this.itemView.setClickable(true);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecViewOnClickListener != null)
                v.requestFocus();
            mRecViewOnClickListener.OnClickListener(v, getPosition());
        }
    }
}
