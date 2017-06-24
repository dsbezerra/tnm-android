package com.tnmlicitacoes.app.ui.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stripe.android.model.Card;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.OnClickListenerRecyclerView;
import com.tnmlicitacoes.app.ui.adapter.holder.SimpleListItemViewHolder;
import com.tnmlicitacoes.app.ui.widget.TnmCardInputWidget;

import java.util.ArrayList;
import java.util.List;


public class PaymentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /* View type count */
    private static int VIEW_TYPE_COUNT                      = 0;

    /* Card view type */
    private static final int VIEW_TYPE_CARD                 = VIEW_TYPE_COUNT++;

    /* Add payment view type */
    private static final int VIEW_TYPE_ADD_PAYMENT          = VIEW_TYPE_COUNT++;

    /* RecyclerView click listener */
    private OnClickListenerRecyclerView mListener;

    /* Holds the card list from Stripe
     * TODO(diego): Replace with Payment class if other forms of payments are implemented
     * */
    private List<Card> mCardList = new ArrayList<>();

    /* The default card id */
    private String mDefaultCard;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder VH;

        if (viewType == VIEW_TYPE_CARD) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.item_card, parent, false);
            VH = new CardItemViewHolder(v);
        } else  {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            SimpleListItemViewHolder holder = new SimpleListItemViewHolder(v);
            holder.setOnClickListener(mListener);
            VH = holder;
        }

        return VH;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Context context = holder.itemView.getContext();

        if (holder instanceof CardItemViewHolder) {
            CardItemViewHolder cardHolder = (CardItemViewHolder) holder;

            Card card = getItem(position);
            if (card != null) {
                cardHolder.cardNumber.setText(context.getString(R.string.card_number_last_digits, card.getLast4()));
                cardHolder.cardBrand.setImageResource(TnmCardInputWidget.BRAND_RESOURCE_MAP.get(card.getBrand()));
                if (card.getId() != null) {
                    cardHolder.cardDefault.setVisibility(card.getId().equals(mDefaultCard) ? View.VISIBLE : View.GONE);
                }
            }

        } else if (holder instanceof SimpleListItemViewHolder) {
            SimpleListItemViewHolder simpleHolder = (SimpleListItemViewHolder) holder;
            simpleHolder.text1.setText(R.string.card_add_new);
            simpleHolder.text1.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        }
    }

    @Override
    public int getItemCount() {
        return mCardList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (getItem(position) != null) {
            return VIEW_TYPE_CARD;
        }

        return VIEW_TYPE_ADD_PAYMENT;
    }

    /**
     * Get a Card item at the given position
     */
    public Card getItem(int position) {
        if (position >= 0 && position < mCardList.size()) {
            return mCardList.get(position);
        }

        return null;
    }

    /**
     * Inserts a new Card in the collection
     * @param card card to be added
     * @param position position where the card is going to be inserted
     */
    public void add(Card card, int position) {
        mCardList.add(card);
        notifyItemInserted(position);
    }

    /**
     * Sets the payment list for this adapter
     */
    public void setPaymentList(List<Card> list) {
        this.mCardList = list;
        notifyDataSetChanged();
    }

    /**
     * Sets the default card
     */
    public void setDefaultCard(String defaultCard) {
        this.mDefaultCard = defaultCard;
        notifyDataSetChanged();
    }

    /**
     * Gets the default card
     */
    public String getDefaultCard() {
        return mDefaultCard;
    }

    /**
     * Sets the click item listener
     */
    public void setOnItemClickListener(OnClickListenerRecyclerView listener) {
        this.mListener = listener;
    }

    private class CardItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView cardDefault;
        private TextView cardNumber;
        private ImageView cardBrand;

        public CardItemViewHolder(View itemView) {
            super(itemView);
            this.cardDefault = (TextView) itemView.findViewById(R.id.card_default);
            this.cardNumber = (TextView) itemView.findViewById(R.id.card_number);
            this.cardBrand = (ImageView) itemView.findViewById(R.id.card_brand);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.OnClickListener(v, getAdapterPosition());
            }
        }
    }
}
