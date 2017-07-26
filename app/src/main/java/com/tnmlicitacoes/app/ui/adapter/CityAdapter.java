package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.tnmlicitacoes.app.CitiesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.SubscriptionPlan;
import com.tnmlicitacoes.app.type.State;
import com.tnmlicitacoes.app.ui.view.NoItemViewHolder;
import com.tnmlicitacoes.app.utils.BillingUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    /* Tag for logging */
    private static final String TAG = "CityAdapter";

    /* View type ids */
    private final int VIEW_NO_ITEM = 0;
    private final int VIEW_ITEM    = 1;

    /* Context of the app */
    private final Context mContext;

    /* Store the fetched cities */
    private List<CitiesQuery.Edge> mCityEdges = Collections.emptyList();
    private List<CitiesQuery.Edge> mCityFilterEdges = Collections.emptyList();
    private CityFilter mCityFilter;

    /* Store the selected cities */
    private HashMap<String, CitiesQuery.Node> mSelectedCities = new HashMap<>();

    /* OnClick listener */
    private OnClickListenerRecyclerView mRecyclerViewOnClickListenerHack;

    /* Max possible cities to pick */
    private int mMax = SubscriptionPlan.BASIC_MAX_QUANTITY;

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

            CitiesQuery.Node city = mCityEdges.get(position).node();
            if (city != null) {
                boolean isSelected = mSelectedCities.containsKey(city.id());

                itemViewHolder.itemCheckBox.setChecked(isSelected);
                if (isSelected) {
                    itemViewHolder.itemView.setBackgroundColor(Color.parseColor("#10ff6600"));
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
                    itemViewHolder.stateName.setText(
                            com.tnmlicitacoes.app.model.State.valueOf(state.name()).toString()
                    );
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

    /**
     * Returns the item in the given position
     */
    public CitiesQuery.Node getItem(int position) {
        if (position >= 0 && position < mCityEdges.size()) {
            return mCityEdges.get(position).node();
        }

        return null;
    }

    /**
     * Sets the city list
     * @param list The CitiesQuery new list
     */
    public void setItems(List<CitiesQuery.Edge> list) {
        mCityEdges = list;
        mCityFilterEdges = list;
        notifyDataSetChanged();
    }

    /**
     * Gets the city list
     * @return the CitiesQuery list
     */
    public List<CitiesQuery.Edge> getItems() {
        return mCityEdges;
    }

    /**
     * Inserts a new CityEdge in the collection
     * @param edge city edge to be added
     * @param position position where the edge is going to be inserted
     */
    public void add(CitiesQuery.Edge edge, int position) {
        mCityEdges.add(edge);
        notifyItemInserted(position);
    }

    /**
     * Removes a CityEdge of the collection
     * @param position position where the edge is going to be removed
     */
    public void remove(int position) {
        mCityEdges.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Add city to selected map
     * @param position position of the touched city in the list
     * @return returns new count on success and -1 on failure
     */
    public int select(int position) {
        int result = -1;

        if (position < 0 || position >= mCityEdges.size()) {
            return result;
        }

        CitiesQuery.Node newCity = mCityEdges.get(position).node();
        if (newCity != null) {
            // Check if is already selected, then remove
            if (mSelectedCities.containsKey(newCity.id())) {
                result = deselect(newCity.id());
            } else {
                // TODO(diego): Check if the user can select
                if (getSelectedCount() < mMax) {
                    mSelectedCities.put(newCity.id(), newCity);
                    result = mSelectedCities.size();
                }
            }

            // Notify the adapter that data has changed
            notifyItemChanged(position);
        }

        return result;
    }

    /**
     * Deselect a given city by ID
     * @param key the id of the city in the map
     * @return new count on success and -1 on failure
     */
    public int deselect(String key) {
        int result = -1;

        if (key == null || key.isEmpty()) {
            return result;
        }

        CitiesQuery.Node removed = mSelectedCities.remove(key);
        if (removed != null) {
            result = mSelectedCities.size();
        }

        return result;
    }

    /**
     * Sets the selected items map
     * @param selected the selected items map
     */
    public void setSelected(HashMap<String, CitiesQuery.Node> selected) {
        this.mSelectedCities = selected;
        notifyDataSetChanged();
    }

    /**
     * Returns the selected cities map
     * @return selected cities
     */
    public HashMap<String, CitiesQuery.Node> getSelected() {
        return mSelectedCities;
    }

    /**
     * Sets the maximum possible to pick segments
     */
    public void setMax(int value) {
        this.mMax = value;
    }

    /**
     * Gets maximum possible to pick segments
     */
    public int getMax() {
        return mMax;
    }

    /**
     * Returns the count of selected cities
     * @return count of selected cities
     */
    public int getSelectedCount() {
        return mSelectedCities.size();
    }

    /**
     * Sets the on click listener
     * @param r the listener
     */
    public void setListenerHack (OnClickListenerRecyclerView r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    @Override
    public Filter getFilter() {
        if (mCityFilter == null) {
            mCityFilter = new CityFilter();
        }
        return mCityFilter;
    }

    /**
     * ViewHolder for the item_city.xml view
     */
    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView cityName;
        private TextView stateName;
        private CheckBox itemCheckBox;

        private VH(View itemView) {
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

    private class CityFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<CitiesQuery.Edge> filterList = new ArrayList<>();
                for (int i = 0; i < mCityFilterEdges.size(); i++) {
                    CitiesQuery.Edge edge = mCityFilterEdges.get(i);
                    CitiesQuery.Node node = edge.node();

                    String name = node.name().toLowerCase();
                    String abbr = node.state().name().toLowerCase();
                    String stateName = com.tnmlicitacoes.app.model.State.valueOf(
                            node.state().name()
                    ).toString().toLowerCase();

                    String constraintLowercased = constraint.toString().toLowerCase();
                    if (name.contains(constraintLowercased) ||
                            abbr.contains(constraintLowercased) ||
                            stateName.contains(constraintLowercased)) {

                        filterList.add(edge);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mCityFilterEdges.size();
                results.values = mCityFilterEdges;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (constraint != null && constraint.length() != 0) {
                mCityEdges = (ArrayList< CitiesQuery.Edge>) results.values;
            } else {
                mCityEdges = mCityFilterEdges;
            }
            notifyDataSetChanged();
        }
    }
}
