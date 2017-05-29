package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.type.State;
import com.tnmlicitacoes.app.ui.view.NoItemViewHolder;
import com.tnmlicitacoes.app.utils.BillingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* Tag for logging */
    private static final String TAG = "CityAdapter";

    /* View type ids */
    private final int VIEW_NO_ITEM = 0;
    private final int VIEW_ITEM    = 1;

    /* Context of the app */
    private final Context mContext;

    /* Store the fetched cities */
    private List<CitiesQuery.Data.Edge> mCityEdges = new ArrayList<>();

    /* Store the selected cities */
    private HashMap<String, CitiesQuery.Data.Node> mSelectedCities = new HashMap<>();

    /* OnClick listener */
    private OnClickListenerRecyclerView mRecyclerViewOnClickListenerHack;

    public CityAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;
        if(viewType == VIEW_ITEM) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_city, parent, false);
            vh = new VH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.no_item_text, parent, false);

            vh = new NoItemViewHolder(v, mRecyclerViewOnClickListenerHack);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof VH) {
            VH itemViewHolder = (VH) holder;

            CitiesQuery.Data.Node city = mCityEdges.get(position).node();
            if (city != null) {
                boolean isSelected = mSelectedCities.containsKey(city.id());

                itemViewHolder.itemCheckBox.setChecked(isSelected);
                if (isSelected) {
                    itemViewHolder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.lightBackground));
                } else {
                    int[] attrs = new int[]{R.attr.selectableItemBackground};
                    TypedArray typedArray = mContext.obtainStyledAttributes(attrs);
                    int backgroundResource = typedArray.getResourceId(0, 0);
                    itemViewHolder.itemView.setBackgroundResource(backgroundResource);
                    typedArray.recycle();
                }

                itemViewHolder.cityName.setText(city.name());

                State state = city.state();
                if (state != null) {
                    itemViewHolder.stateName.setText(state.name());
                }

                itemViewHolder.itemCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRecyclerViewOnClickListenerHack.OnClickListener(v, holder.getAdapterPosition());
                    }
                });
            }

        } else {
            NoItemViewHolder itemViewHolder = (NoItemViewHolder) holder;
            SpannableString content = new SpannableString(mContext.getString(R.string.city_select_text_no_city));
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
            itemViewHolder.noItemTv.setText(content);
            itemViewHolder.noItemTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRecyclerViewOnClickListenerHack.OnClickListener(v, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mCityEdges.get(position) != null ? VIEW_ITEM : VIEW_NO_ITEM;
    }

    /**
     * Returns the count of items in adapter
     * @return count o items in adapter
     */
    @Override
    public int getItemCount() {
        return mCityEdges.size();
    }

    public CitiesQuery.Data.Node getItem(int position) {
        return mCityEdges.get(position).node();
    }

    /**
     * Returns the count of selected cities
     * @return count of selected cities
     */
    public int getSelectedCount() {
        return mSelectedCities.size();
    }

    public void setItems(List<CitiesQuery.Data.Edge> list) {
        mCityEdges = list;
        notifyDataSetChanged();
    }

    /**
     * Inserts an new CityEdge in the collection
     * @param edge city edge to be added
     * @param position position where the edge is going to be inserted
     */
    public void add(CitiesQuery.Data.Edge edge, int position) {
        mCityEdges.add(edge);
        notifyItemInserted(position);
    }

    /**
     * Add city to selected collection
     * @param position position of the touched city in the list
     * @return returns new count on success and -1 on failure
     */
    public int addToSelected(int position) {

        int result = -1;

        CitiesQuery.Data.Node newCity = mCityEdges.get(position).node();
        if (newCity != null) {
            // Check if is already selected, then remove
            if (mSelectedCities.containsKey(newCity.id())) {
                mSelectedCities.remove(newCity.id());
                result = mSelectedCities.size();
            } else {

                if (getSelectedCount() < BillingUtils.SUBSCRIPTION_MAX_ITEMS) {
                    mSelectedCities.put(newCity.id(), newCity);
                    result = mSelectedCities.size();
                }
            }

            // Notify the adapter that data has changed
            notifyItemChanged(position);
        }

        return result;
    }

    public void setSelected(HashMap<String, CitiesQuery.Data.Node> selected) {
        this.mSelectedCities = selected;
        notifyDataSetChanged();
    }

    /**
     * Sets the on click listener
     * @param r the listener
     */
    public void setListenerHack (OnClickListenerRecyclerView r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    /**
     * ViewHolder for the item_city.xml view
     */
    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView cityName;
        public TextView stateName;
        public CheckBox itemCheckBox;

        public VH(View itemView) {
            super(itemView);
            this.cityName = (TextView) itemView.findViewById(R.id.cityName);
            this.stateName = (TextView) itemView.findViewById(R.id.stateName);
            this.itemCheckBox = (CheckBox) itemView.findViewById(R.id.itemCheckBox);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null) {
                v.requestFocus();
            }
            itemCheckBox.performClick();
        }
    }
}
