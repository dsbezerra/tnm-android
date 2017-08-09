package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tnmlicitacoes.app.Config;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.apollo.SegmentsQuery;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.model.SubscriptionPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SegmentAdapter extends RecyclerView.Adapter<SegmentAdapter.VH> implements Filterable {

    /** Tag for logging */
    private static final String TAG = "SegmentAdapter";

    /** The uris */
    private static final String BACKGROUND = Config.TNM_URL_PREFIX + "/img/app/segments/${id}/background_";
    private static final String LOW_MEDIUM_BACKGROUND = BACKGROUND + "1x.webp";
    private static final String HIGH_BACKGROUND = BACKGROUND + "1.5x.webp";
    private static final String XHIGH_BACKGROUND = BACKGROUND + "2x.webp";
    private static final String XXHIGH_BACKGROUND = BACKGROUND + "3x.webp";
    private static final String XXXHIGH_BACKGROUND = BACKGROUND + "4x.webp";

    /** Placeholder colors */
    private static final ColorDrawable[] PLACEHOLDER_COLORS = {
            // Alimentação
            new ColorDrawable(Color.parseColor("#C66526")),
            // Armazém e Estoque de Materiais
            new ColorDrawable(Color.parseColor("#A14F24")),
            // Construção Civil
            new ColorDrawable(Color.parseColor("#A05B31")),
            // Consultoria/RH
            new ColorDrawable(Color.parseColor("#59494C")),
            // Eletrodomésticos e Utensílios
            new ColorDrawable(Color.parseColor("#925424")),
            // Embalagem e Descartáveis
            new ColorDrawable(Color.parseColor("#5C5452")),
            // Esportivo e Recreativo
            new ColorDrawable(Color.parseColor("#833821")),
            // Eventos
            new ColorDrawable(Color.parseColor("#B4ACAC")),
            // Gráfica
            new ColorDrawable(Color.parseColor("#D78020")),
            // Hospedagem/Viagem
            new ColorDrawable(Color.parseColor("#8475A5")),
            // Informática e Telecomunicação
            new ColorDrawable(Color.parseColor("#50449C")),
            // Instrumentos Musicais
            new ColorDrawable(Color.parseColor("#9A471D")),
            // Jardinagem e Paisagismo
            new ColorDrawable(Color.parseColor("#C4C43F")),
            // Locação de Imóveis
            new ColorDrawable(Color.parseColor("#CDB393")),
            // Locação/Venda de Veículos
            new ColorDrawable(Color.parseColor("#47363A")),
            // Máquinas e Equipamentos
            new ColorDrawable(Color.parseColor("#746264")),
            // Móveis
            new ColorDrawable(Color.parseColor("#BAA493")),
            // Papelaria
            new ColorDrawable(Color.parseColor("#778C46")),
            // Produtos Infantis
            new ColorDrawable(Color.parseColor("#C59C17")),
            // Publicidade
            new ColorDrawable(Color.parseColor("#B18252")),
            // Saúde
            new ColorDrawable(Color.parseColor("#846bE9")),
            // Seguros
            new ColorDrawable(Color.parseColor("#5A54B2")),
            // Serviços Administrativos
            new ColorDrawable(Color.parseColor("#ACA4A4")),
            // Serviços de Engenharia
            new ColorDrawable(Color.parseColor("#8F6140")),
            // Serviços de Limpeza
            new ColorDrawable(Color.parseColor("#CBD559")),
            // Serviços de Refrigeração
            new ColorDrawable(Color.parseColor("#645389")),
            // Serviços/Produtos de Segurança
            new ColorDrawable(Color.parseColor("#453227")),
            // Serviços/Produtos de Veículos
            new ColorDrawable(Color.parseColor("#B8443D")),
            // Supermercado
            new ColorDrawable(Color.parseColor("#9664B8")),
            // Transportes e Combustíveis
            new ColorDrawable(Color.parseColor("#6651B0")),
            // Químicos
            new ColorDrawable(Color.parseColor("#8C7C7C")),
            // Vestuário
            new ColorDrawable(Color.parseColor("#4E3632"))
    };

    /* Context of the app */
    private final Context mContext;

    /* Store the fetched segments */
    private List<SegmentsQuery.Edge> mSegmentEdges = new ArrayList<>();
    private List<SegmentsQuery.Edge> mSegmentFilterEdges = new ArrayList<>();

    private SegmentFilter mSegmentFilter;

    /* Store the selected segments */
    private HashMap<String, SegmentsQuery.Node> mSelectedSegments = new HashMap<>();

    /* Holds the dpi density of the device */
    private final int mDensityDpi;

    /* OnClick listener */
    private OnClickListenerRecyclerView mRecyclerViewOnClickListenerHack;

    /* Max possible segments to pick */
    private int mMax = SubscriptionPlan.BASIC_MAX_QUANTITY;

    /**
     * Indicate we should hide checkbox or not
     **/
    private boolean mShouldHideCheckbox = false;

    public SegmentAdapter(Context context) {
        mContext = context;
        mDensityDpi = context.getResources().getDisplayMetrics().densityDpi;
    }

    @Override
    public SegmentAdapter.VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_segment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(final VH holder, final int position) {
        SegmentsQuery.Node segment = getItem(position);
        if (segment != null) {

            boolean isSelected = mSelectedSegments.containsKey(segment.id());

            holder.segmentName.setSelected(true);

            if (mShouldHideCheckbox) {
                holder.checkBox.setVisibility(View.GONE);
            } else {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(true);

                holder.checkBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRecyclerViewOnClickListenerHack.OnClickListener(v, position);
                    }
                });
            }

            if (isSelected) {
                holder.effect.setBackground(new ColorDrawable(Color.parseColor("#10ff6600")));
                holder.checkBox.setChecked(true);
            } else {
                holder.effect.setBackground(new ColorDrawable(Color.parseColor("#10000000")));
                holder.checkBox.setChecked(false);
            }


            // Load background
            Picasso.with(mContext)
                    .load(getUriAccordingWithDpi(segment))
                    .placeholder(getPlaceholderColor(position))
                    .into(holder.segmentBackground);
            // Set text
            holder.segmentName.setText(segment.name());

            String icon = segment.icon();
            if (icon == null) {
                icon = "/img/app/segments/${id}/icon_white.webp".replace("${id}", segment.id());
            }
            // Load icon with picasso
            Picasso.with(mContext)
                    .load(Config.TNM_URL_PREFIX + icon)
                    .into(holder.segmentIcon);
        }
    }

    private ColorDrawable getPlaceholderColor(int position) {
        if (position >= 0 && position < PLACEHOLDER_COLORS.length) {
            return PLACEHOLDER_COLORS[position];
        }

        return new ColorDrawable(Color.parseColor("#FFFFFF"));
    }

    @Override
    public int getItemCount() {
        return mSegmentEdges.size();
    }

    /**
     * Returns the item in the given position
     */
    public SegmentsQuery.Node getItem(int position) {
        if (position >= 0 && position < mSegmentEdges.size()) {
            return mSegmentEdges.get(position).node();
        }

        return null;
    }

    /**
     * Sets the segment list
     * @param list The SegmentsQuery new list
     */
    public void setItems(List<SegmentsQuery.Edge> list) {
        mSegmentEdges = list;
        mSegmentFilterEdges = list;
        notifyDataSetChanged();
    }

    /**
     * Gets the segment list
     * @return the SegmentsQuery list
     */
    public List<SegmentsQuery.Edge> getItems() {
        return mSegmentEdges;
    }

    /**
     * Inserts a new SegmentEdge in the collection
     * @param edge segment edge to be added
     * @param position position where the edge is going to be inserted
     */
    public void add(SegmentsQuery.Edge edge, int position) {
        mSegmentEdges.add(edge);
        notifyItemInserted(position);
    }

    /**
     * Removes a SegmentEdge of the collection
     * @param position position where the edge is going to be removed
     */
    public void remove(int position) {
        mSegmentEdges.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Add segment to selected collection
     * @param position position of the touched segment in the list
     * @return returns new count on success and -1 on failure
     */
    public int select(int position) {
        int result = -1;

        if (position < 0 || position >= mSegmentEdges.size()) {
            return result;
        }

        SegmentsQuery.Node newSegment = mSegmentEdges.get(position).node();
        if (newSegment != null) {
            // Check if is already selected, then remove
            if (mSelectedSegments.containsKey(newSegment.id())) {
                result = deselect(newSegment.id());
            } else {
                // TODO(diego): Check if the user can select
                if (getSelectedCount() < mMax) {
                    mSelectedSegments.put(newSegment.id(), newSegment);
                    result = mSelectedSegments.size();
                }
            }

            // Notify the adapter that data has changed
            notifyItemChanged(position);
        }

        return result;
    }

    /**
     * Deselect a given segment by ID
     * @param key the id of the segment in the map
     * @return new count on success and -1 on failure
     */
    private int deselect(String key) {
        int result = -1;

        if (key == null || key.isEmpty()) {
            return result;
        }

        SegmentsQuery.Node removed = mSelectedSegments.remove(key);
        if (removed != null) {
            result = mSelectedSegments.size();
        }

        return result;
    }

    /**
     * Sets the selected items map
     * @param selected the selected items map
     */
    public void setSelected(HashMap<String, SegmentsQuery.Node> selected) {
        this.mSelectedSegments = selected;
        notifyDataSetChanged();
    }

    /**
     * Returns the selected segments map
     * @return selected segments
     */
    public HashMap<String, SegmentsQuery.Node> getSelected() {
        return mSelectedSegments;
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
     * Returns the count of selected segments
     * @return count of selected segments
     */
    public int getSelectedCount() {
        return mSelectedSegments.size();
    }

    /**
     * Sets the on click listener
     * @param r the listener
     */
    public void setListenerHack (OnClickListenerRecyclerView r) {
        mRecyclerViewOnClickListenerHack = r;
    }

    /**
     * Sets the highlight new value
     */
    public void setIsHighlight(boolean value) {
        this.mShouldHideCheckbox = value;
    }

    /**
     * Returns the background image uri according with the density dpi of device
     * @param segment The segment to get the uri
     * @return uri for the density
     */
    private String getUriAccordingWithDpi(SegmentsQuery.Node segment) {

        String result = null;

        switch (mDensityDpi) {

            case DisplayMetrics.DENSITY_LOW:
            case DisplayMetrics.DENSITY_MEDIUM:
                result = LOW_MEDIUM_BACKGROUND;
                break;

            case DisplayMetrics.DENSITY_HIGH:
                result = HIGH_BACKGROUND;
                break;

            case DisplayMetrics.DENSITY_XHIGH:
                result = XHIGH_BACKGROUND;
                break;

            case DisplayMetrics.DENSITY_XXHIGH:
                result = XXHIGH_BACKGROUND;
                break;

            case DisplayMetrics.DENSITY_XXXHIGH:
                result = XXXHIGH_BACKGROUND;
                break;

            default:
                result = LOW_MEDIUM_BACKGROUND;
                break;
        }

        result = result.replace("${id}", segment.id());

        return result;
    }

    @Override
    public Filter getFilter() {
        if (mSegmentFilter == null) {
            mSegmentFilter = new SegmentFilter();
        }
        return mSegmentFilter;
    }

    /**
     * ViewHolder for the item_city.xml view
     */
    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View effect;
        private CheckBox checkBox;
        private TextView segmentName;
        private ImageView segmentIcon;
        private ImageView segmentBackground;

        private VH(View itemView) {
            super(itemView);
            this.effect = itemView.findViewById(R.id.background_effect);
            this.checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
            this.segmentName = (TextView) itemView.findViewById(R.id.segmentName);
            this.segmentIcon = (ImageView) itemView.findViewById(R.id.segmentIcon);
            this.segmentBackground = (ImageView) itemView.findViewById(R.id.segmentBackground);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mRecyclerViewOnClickListenerHack != null) {
                v.requestFocus();
                checkBox.performClick();
            }
        }
    }

    private class SegmentFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<SegmentsQuery.Edge> filterList = new ArrayList<>();
                for (int i = 0; i < mSegmentFilterEdges.size(); i++) {
                    SegmentsQuery.Edge edge = mSegmentFilterEdges.get(i);
                    SegmentsQuery.Node node = edge.node();

                    String name = node.name().toLowerCase();
                    String constraintLowercased = constraint.toString().toLowerCase();
                    if (name.contains(constraintLowercased)) {
                        filterList.add(edge);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mSegmentFilterEdges.size();
                results.values = mSegmentFilterEdges;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (constraint != null && constraint.length() != 0) {
                mSegmentEdges = (ArrayList<SegmentsQuery.Edge>) results.values;
            } else {
                mSegmentEdges = mSegmentFilterEdges;
            }
            notifyDataSetChanged();
        }
    }
}
