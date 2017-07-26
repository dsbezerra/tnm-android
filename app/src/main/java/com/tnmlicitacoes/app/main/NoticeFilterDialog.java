package com.tnmlicitacoes.app.main;

import android.os.Bundle;
import android.support.annotation.StringDef;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.interfaces.FilterListener;
import com.tnmlicitacoes.app.model.realm.PickedCity;
import com.tnmlicitacoes.app.ui.base.BaseDialogFragment;
import com.tnmlicitacoes.app.utils.NoticeUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;

public class NoticeFilterDialog extends BaseDialogFragment {

    public static final String TAG = "NoticeFilterDialog";

    private Realm mRealm;

    private Button mApply;
    private Button mCancel;

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            FILTER_MODALITY,
            FILTER_REGION,
            FILTER_EXCLUSIVE
    })
    public @interface FilterParam { }
    public static final String FILTER_MODALITY = "modality";
    public static final String FILTER_REGION = "cityId";
    public static final String FILTER_EXCLUSIVE = "exclusive";
    private HashMap<String, Object> mFilterParams = new HashMap<>();

    private Spinner mModalitySpinner;
    private SpinnerItemAdapter mModalityAdapter;
    private String[] mModalities;

    private Spinner mRegionSpinner;
    private SpinnerItemAdapter mRegionAdapter;
    private RealmResults<PickedCity> mRegions;

    private CheckBox mExclusive;

    private FilterListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRealm = Realm.getDefaultInstance();
        // Set title for this dialog
        getDialog().setTitle("Filtro");

        View v = inflater.inflate(R.layout.dialog_filter, container, false);
        initViews(v);
        initListeners();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRealm.close();
    }

    private void initViews(View view) {
        mModalitySpinner = (Spinner) view.findViewById(R.id.modality_spinner);
        mRegionSpinner = (Spinner) view.findViewById(R.id.region_spinner);
        mExclusive = (CheckBox) view.findViewById(R.id.exclusive_checkbox);

        mApply = (Button) view.findViewById(R.id.apply);
        mCancel = (Button) view.findViewById(R.id.cancel);

        mModalityAdapter =  new SpinnerItemAdapter(getModalityItems());
        mRegionAdapter = new SpinnerItemAdapter(getRegionItems());

        mModalitySpinner.setAdapter(mModalityAdapter);
        mRegionSpinner.setAdapter(mRegionAdapter);
    }

    private void initListeners() {
        mApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFilter(mFilterParams);
                    dismiss();
                }
            }
        });
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mModalitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleModalityFilter(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });

        mRegionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                handleRegionFilter(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No-op
            }
        });

        mExclusive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mFilterParams.put(FILTER_EXCLUSIVE, isChecked);
            }
        });
    }

    public NoticeFilterDialog withListener(FilterListener listener) {
        this.mListener = listener;
        return this;
    }

    /**
     * Handles modality filter selection
     */
    private void handleModalityFilter(int position) {
        // All modalities
        if (position == 0) {
            if (mFilterParams.containsKey(FILTER_MODALITY)) {
                mFilterParams.remove(FILTER_MODALITY);
            }

            return;
        }


        String modality = mModalities[position];
        mFilterParams.put(FILTER_MODALITY, NoticeUtils.resolveNameToModality(modality, false).name());
    }

    /**
     * Handles the region filter
     */
    private void handleRegionFilter(int position) {
        // All cities
        if (position == 0) {
            if (mFilterParams.containsKey(FILTER_REGION)) {
                mFilterParams.remove(FILTER_REGION);
            }

            return;
        }

        int regionPos = position - 1;
        if (regionPos < 0 || regionPos >= mRegions.size()) {
            return;
        }

        String cityId = mRegions.get(regionPos).getId();
        mFilterParams.put(FILTER_REGION, cityId);
    }

    /**
     * Get the list of items to display in the region spinner
     * @return
     */
    private String[] getRegionItems() {
        mRegions = mRealm.where(PickedCity.class)
                .findAll()
                .sort("name");

        String[] result = new String[mRegions.size() + 1];
        result[0] = "Todas as cidades escolhidas";
        for (int i = 1; i < result.length; i++) {
            result[i] = mRegions.get(i - 1).getName();
        }

        return result;
    }

    /**
     * Get the list of items to display in the modality spinner
     */
    private String[] getModalityItems() {
        mModalities = getResources()
                .getStringArray(R.array.filter_modalities);
        return mModalities;
    }

    /**
     * SimpleSpinnerItem with a little more of padding
     */
    public class SpinnerItemAdapter extends BaseAdapter {

        private String[] mItems;

        public SpinnerItemAdapter(String[] items) {
            this.mItems = items;
        }

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = View.inflate(getActivity(), R.layout.item_spinner, null);
            TextView itemName = (TextView) convertView.findViewById(R.id.itemName);
            itemName.setText(mItems[position]);
            return convertView;
        }
    }
}
