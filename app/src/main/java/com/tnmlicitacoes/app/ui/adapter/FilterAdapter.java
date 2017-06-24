package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.model.filter.FilterItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diegobezerra on 5/31/17.
 */

public class FilterAdapter extends ArrayAdapter<FilterItem> {

    private Context mContext;

    private List<FilterItem> mFilterItems = new ArrayList<>();

    public FilterAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
        mContext = context;
    }

    public FilterAdapter(Context context, List<FilterItem> items) {
        super(context, R.layout.item_filter);
        mContext = context;
        mFilterItems = items;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= 0 && position < mFilterItems.size()) {
            return mFilterItems.get(position).getType();
        }
        return super.getItemViewType(position);
    }

    @Nullable
    @Override
    public FilterItem getItem(int position) {
        if (position >= 0 && position < mFilterItems.size()) {
            return mFilterItems.get(position);
        }
        return null;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

        FilterItem item = getItem(position);

        int viewType = getItemViewType(position);
        if (viewType == FilterItem.FilterType.LABEL) {
            FilterLabelViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_filter_label, parent, false);
                holder = new FilterLabelViewHolder();
                holder.label = (TextView) view.findViewById(R.id.label);
                view.setTag(holder);
            } else {
                holder = (FilterLabelViewHolder) view.getTag();
            }
            holder.label.setText(item.getLabel());

        } else if (viewType == FilterItem.FilterType.VALUE) {
            FilterValueViewHolder holder;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_filter, parent, false);
                holder = new FilterValueViewHolder();
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.checkBox = (CheckBox) view.findViewById(R.id.checkbox);

                view.setTag(holder);
            } else {
                holder = (FilterValueViewHolder) view.getTag();
            }

            holder.name.setText(item.getValue());
        }

        return view;
    }

    private static class FilterValueViewHolder {
        TextView name;
        CheckBox checkBox;
    }

    private static class FilterLabelViewHolder {
        TextView label;
    }
}
