package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.tnmlicitacoes.app.NoticesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.utils.NoticeUtils;

import java.util.ArrayList;
import java.util.List;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class NoticeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* Tag for logging */
    private static final String TAG = "NoticeAdapter";

    /* Icon of segment TODO(diego): See if this is still relevant */
    private static String sIconUrl = "https://tnmlicitacoes.com/img/app/categories/{segId}/icon.png";

    /* View type ids */
    private final int VIEW_LOADING_MORE = 0;
    private final int VIEW_ITEM         = 1;

    /* Context of the app */
    private final Context mContext;

    /* Store the fetched notices */
    private List<NoticesQuery.Data.Edge> mNoticeEdges = new ArrayList<>();

    /* OnClick listener */
    private OnClickListenerRecyclerView mOnClickListener;


    // TODO(diego): See if this is still relevant
    //private NoticeFilter mNoticeFilter = null;

    public NoticeAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        return mNoticeEdges.get(position) != null ? VIEW_ITEM : VIEW_LOADING_MORE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if(viewType == VIEW_ITEM) {
            holder = new NoticeViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notice, parent, false));
        }
        else {
            holder = new ProgressViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progress_item, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof NoticeViewHolder) {

            final NoticeViewHolder noticeHolder = (NoticeViewHolder) holder;

            final NoticesQuery.Data.Node notice = getItem(position);
            if (notice != null) {

                if (notice.agency() != null) {
                    NoticesQuery.Data.Agency agency = notice.agency();
                    noticeHolder.noticeAgencyInfo.setText(mContext.getResources().getString(
                            R.string.notices_item_header, agency.city().state().name(),
                            agency.city().name(), agency.name()));
                }

                noticeHolder.noticeModality.setText(
                        NoticeUtils.resolveModalityToName(notice.modality())
                );
                noticeHolder.noticeNumber.setText(notice.number());
                noticeHolder.noticeDescription.setText(notice.object());

                noticeHolder.exclusiveMpe.setVisibility(notice.exclusive() ? View.VISIBLE : View.GONE);

                String iconUrl = sIconUrl.replace("{segId}", notice.segment().id());
                Picasso.with(mContext).load(iconUrl).into(noticeHolder.noticeSegment, new Callback() {
                    @Override
                    public void onSuccess() {
                        noticeHolder.noticeSegment.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onError() {
                        noticeHolder.noticeSegment.setVisibility(View.GONE);
                    }
                });

                noticeHolder.noticeDisputeDate.setText(notice.disputeDate().toString());
            }



        } else if (holder instanceof ProgressViewHolder){
            LOG_DEBUG(TAG, "Showing progress bar");
        }
    }

    public NoticesQuery.Data.Node getItem(int position) {
        if (position >= 0 && position < mNoticeEdges.size()) {
            return mNoticeEdges.get(position).node();
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return mNoticeEdges.size();
    }

    public void append(List<NoticesQuery.Data.Edge> list) {
        for (int i = 0; i < list.size(); i++) {
            add(list.get(i), mNoticeEdges.size());
        }
    }

    public void setItems(List<NoticesQuery.Data.Edge> list) {
        mNoticeEdges = list;
        notifyDataSetChanged();
    }

    public void add(NoticesQuery.Data.Edge notice, int position) {
        mNoticeEdges.add(notice);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mNoticeEdges.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnClickListener (OnClickListenerRecyclerView listener) {
        mOnClickListener = listener;
    }

    /*public class NoticeFilter extends Filter {

        private List<Notice> mCompleteList;

        public NoticeFilter(List<Notice> completeList) {
            this.mCompleteList = completeList;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            ArrayList<Notice> filtered = new ArrayList<>();
            ArrayList<Notice> localItems = new ArrayList<>();

            if(TextUtils.isEmpty(constraint)) {
                results.values = mCompleteList;
                results.count = mCompleteList.size();
            } else {
                localItems.addAll(mCompleteList);
                for(Notice notice : localItems) {
                    if(notice.description.toLowerCase()
                            .contains(constraint.toString().toLowerCase())) {
                        filtered.add(notice);
                    }
                }
                results.values = filtered;
                results.count = filtered.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            setNoticesList((List<Notice>) results.values);
        }
    }*/

    public class NoticeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public View itemView;
        public ImageView exclusiveMpe;
        public ImageView noticeSegment;
        public TextView noticeAgencyInfo;
        public TextView noticeModality;
        public TextView noticeNumber;
        public TextView noticeDescription;
        public TextView noticeDisputeDate;

        public NoticeViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.exclusiveMpe = (ImageView) itemView.findViewById(R.id.exclusiveMpe);
            this.noticeSegment = (ImageView) itemView.findViewById(R.id.noticeInfoCategory);
            this.noticeAgencyInfo = (TextView) itemView.findViewById(R.id.agencyName);
            this.noticeModality = (TextView) itemView.findViewById(R.id.noticeModality);
            this.noticeNumber = (TextView) itemView.findViewById(R.id.noticeNumber);
            this.noticeDescription = (TextView) itemView.findViewById(R.id.noticeDescription);
            this.noticeDisputeDate = (TextView) itemView.findViewById(R.id.noticeOpenDate);
            this.itemView.setClickable(true);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mOnClickListener != null)
                v.requestFocus();
            mOnClickListener.OnClickListener(v, getPosition());
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public ProgressViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.itemProgressBar);
        }
    }

}
