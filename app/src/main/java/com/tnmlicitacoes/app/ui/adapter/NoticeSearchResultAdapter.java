package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.model.realm.Notice;
import com.tnmlicitacoes.app.utils.DateUtils;
import com.tnmlicitacoes.app.utils.NoticeUtils;

import java.util.ArrayList;
import java.util.List;

public class NoticeSearchResultAdapter extends ArrayAdapter<Notice> {

    /** The notices list */
    private List<Notice> mNoticeList = new ArrayList<>();

    /** The searching query */
    private String mQuery;

    /* Icon of segment TODO(diego): See if this is still relevant */
    private static String sIconUrl = "https://tnmlicitacoes.com/img/app/segments/{segId}/icon_white.webp";

    private ForegroundColorSpan mHighlightColor;

    public NoticeSearchResultAdapter(@NonNull Context context) {
        super(context, 0);
        mHighlightColor = new ForegroundColorSpan(ContextCompat.getColor(context,
                R.color.colorPrimary));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_notice_search_result, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }  else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Notice item = getItem(position);
        if (item != null) {
            Picasso.with(getContext())
                    .load(sIconUrl.replace("{segId}", item.getSegId()))
                    .fit()
                    .into(viewHolder.segment);

            viewHolder.segment.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorLightGrey));
            viewHolder.modality.setText(NoticeUtils.resolveEnumNameToName(item.getModality()));
            viewHolder.number.setText(item.getNumber());
            viewHolder.object.setText(getHighlightedString(item.getObject()));
            viewHolder.disputeDate.setText(DateUtils.format(item.getDisputeDate()));
        }

        return convertView;
    }

    @Nullable
    @Override
    public Notice getItem(int position) {
        if (position >= 0 && position < mNoticeList.size()) {
            return mNoticeList.get(position);
        }

        return null;
    }

    @Override
    public int getCount() {
        return this.mNoticeList.size();
    }

    private SpannableString getHighlightedString(String object) {

        SpannableString result = new SpannableString(object);
        if (TextUtils.isEmpty(mQuery)) {
            return result;
        }

        String lcObject = object.toLowerCase();
        String lcQuery = mQuery.toLowerCase();

        List<Indexes> indexes = findAllIndexes(lcQuery, lcObject);
        for (int i = 0; i < indexes.size(); i++) {
            Indexes item = indexes.get(i);
            result.setSpan(mHighlightColor, item.start, item.end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return result;
    }


    /**
     * TODO(diego): Optimize this by looking for matches only in the visible part of the object string
     */
    private List<Indexes> findAllIndexes(String lcQuery, String lcObject) {
        List<Indexes> result = new ArrayList<>();

        int fromIndex = 0;
        while (fromIndex < lcObject.length()) {
            int start = lcObject.indexOf(lcQuery, fromIndex);
            if (start < 0) {
                break;
            } else {
                int end = start + lcQuery.length();
                result.add(new Indexes(start, end));
                fromIndex = end;
            }
        }

        return result;
    }

    public void setItems(List<Notice> notices) {
        this.mNoticeList = notices;
        notifyDataSetChanged();
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    static class ViewHolder {

        public ImageView segment;

        public TextView modality;
        public TextView number;
        public TextView object;
        public TextView disputeDate;

        public ViewHolder(View v) {
            segment = (ImageView) v.findViewById(R.id.segment_icon);
            modality = (TextView) v.findViewById(R.id.modality);
            number = (TextView) v.findViewById(R.id.number);
            object = (TextView) v.findViewById(R.id.object);
            disputeDate = (TextView) v.findViewById(R.id.dispute_date);
        }
    }

    class Indexes {

        int start;
        int end;

        int length;

        public Indexes(int start, int end) {
            this.start = start;
            this.end = end;
            this.length = start + end;
        }
    }
}
