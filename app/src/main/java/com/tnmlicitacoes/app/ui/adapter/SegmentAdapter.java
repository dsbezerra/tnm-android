package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.tnmlicitacoes.app.Config;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.SegmentsQuery;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.utils.BillingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SegmentAdapter extends RecyclerView.Adapter<SegmentAdapter.VH> {

    /* Tag for logging */
    private static final String TAG = "SegmentAdapter";

    /**
     * Color matrix that flips the components (-1.0f * c + 255 = 255 - c)
     * and keeps the alpha intact.
     */
    private static final float[] NEGATIVE = {
            -1.0f,   0.0f,   0.0f,  0.0f,   255, // red
             0.0f,  -1.0f,   0.0f,  0.0f,   255, // green
             0.0f,   0.0f,  -1.0f,  0.0f,   255, // blue
             0.0f,   0.0f,   0.0f,  1.0f,  0.0f  // alpha
    };

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
    private List<SegmentsQuery.Data.Edge> mSegmentEdges = new ArrayList<>();

    /* Store the selected segments */
    private HashMap<String, SegmentsQuery.Data.Node> mSelectedSegments = new HashMap<>();

    /* Color matrix used to modify color of segment views */
    private final ColorMatrix mColorMatrix = new ColorMatrix();

    /* Holds the dpi density of the device */
    private final int mDensityDpi;

    /* OnClick listener */
    private OnClickListenerRecyclerView mRecyclerViewOnClickListenerHack;

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
    public void onBindViewHolder(final VH holder, int position) {
        SegmentsQuery.Data.Node segment = getItem(position);
        if (segment != null) {

            boolean isSelected = mSelectedSegments.containsKey(segment.id());

            holder.segmentName.setSelected(true);

            Matrix bigMatrix = new Matrix();
            bigMatrix.setScale(5, 5);
            holder.segmentBackground.setImageMatrix(bigMatrix);

            if (!isSelected) {
                mColorMatrix.setSaturation(0.0f);

                // Invert icon colors
                holder.segmentIcon.setColorFilter(new ColorMatrixColorFilter(NEGATIVE));
                holder.segmentName.setTextColor(ContextCompat.getColor(mContext,
                        android.R.color.black));
                holder.segmentGradientEffect.setBackground(null);

                // Display background with colors when selected and P&B with a low alpha if not selected
                Paint paint = new Paint();
                ColorMatrixColorFilter cmColorFilter = new ColorMatrixColorFilter(mColorMatrix);
                paint.setColorFilter(cmColorFilter);
                paint.setAlpha(100);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    holder.segmentBackground.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
                } else {
                    holder.segmentBackground.setColorFilter(cmColorFilter);
                }

            } else {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    holder.segmentBackground.setLayerType(View.LAYER_TYPE_HARDWARE, new Paint());
                } else {
                    mColorMatrix.setSaturation(1.0f);
                    holder.segmentBackground.setColorFilter(new ColorMatrixColorFilter(mColorMatrix));
                }


                // Invert icon colors
                holder.segmentIcon.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix()));
                holder.segmentName.setTextColor(ContextCompat.getColor(mContext, android.R.color.white));

                holder.segmentGradientEffect.setBackground(ContextCompat.getDrawable(mContext,
                        R.drawable.segment_gradient_effect));
            }

            // Load background
            Picasso.with(mContext)
                    .load(getUriAccordingWithDpi(segment))
                    .placeholder(PLACEHOLDER_COLORS[position])
                    .into(holder.segmentBackground);
            // Set text
            holder.segmentName.setText(segment.name());
            // Load icon with picasso
            Picasso.with(mContext)
                    .load(Config.TNM_URL_PREFIX + segment.icon())
                    .into(holder.segmentIcon);
        }
    }

    @Override
    public int getItemCount() {
        return mSegmentEdges.size();
    }

    /**
     * Returns the count of selected segments
     * @return count of selected segments
     */
    public int getSelectedCount() {
        return mSelectedSegments.size();
    }

    public SegmentsQuery.Data.Node getItem(int position) {
        if (position >= 0 && position < mSegmentEdges.size()) {
            return mSegmentEdges.get(position).node();
        } else {
            return null;
        }
    }

    public void setItems(List<SegmentsQuery.Data.Edge> list) {
        mSegmentEdges = list;
        notifyDataSetChanged();
    }

    /**
     * Add segment to selected collection
     * @param position position of the touched segment in the list
     * @return returns new count on success and -1 on failure
     */
    public int addToSelected(int position) {

        int result = -1;

        SegmentsQuery.Data.Node newSegment = mSegmentEdges.get(position).node();
        if (newSegment != null) {
            // Check if is already selected, then remove
            if (mSelectedSegments.containsKey(newSegment.id())) {
                mSelectedSegments.remove(newSegment.id());
                result = mSelectedSegments.size();
            } else {

                if (getSelectedCount() < BillingUtils.SUBSCRIPTION_MAX_ITEMS) {
                    mSelectedSegments.put(newSegment.id(), newSegment);
                    result = mSelectedSegments.size();
                }
            }

            // Notify the adapter that data has changed
            notifyItemChanged(position);
        }

        return result;
    }

    public void setSelected(HashMap<String, SegmentsQuery.Data.Node> selected) {
        this.mSelectedSegments = selected;
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
     * Returns the background image uri according with the density dpi of device
     * @param segment
     * @return
     */
    private String getUriAccordingWithDpi(SegmentsQuery.Data.Node segment) {
        String result = Config.TNM_URL_PREFIX + segment.defaultImg();
        if (mDensityDpi >= DisplayMetrics.DENSITY_HIGH && mDensityDpi <= DisplayMetrics.DENSITY_XHIGH) {
            result = Config.TNM_URL_PREFIX + segment.mqdefault();
        }
        else if (mDensityDpi >= DisplayMetrics.DENSITY_XXHIGH && mDensityDpi <= DisplayMetrics.DENSITY_XXXHIGH) {
            result = Config.TNM_URL_PREFIX + segment.hqdefault();
        }
        return result;
    }

    /**
     * ViewHolder for the item_city.xml view
     */
    public class VH extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView segmentName;
        public ImageView segmentIcon;
        public ImageView segmentBackground;
        public View segmentGradientEffect;

        public VH(View itemView) {
            super(itemView);
            this.segmentName = (TextView) itemView.findViewById(R.id.segmentName);
            this.segmentIcon = (ImageView) itemView.findViewById(R.id.segmentIcon);
            this.segmentBackground = (ImageView) itemView.findViewById(R.id.segmentBackground);
            this.segmentGradientEffect = itemView.findViewById(R.id.segmentGradientEffect);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(mRecyclerViewOnClickListenerHack != null) {
                v.requestFocus();
                mRecyclerViewOnClickListenerHack.OnClickListener(v, getAdapterPosition());
            }
        }
    }
}
