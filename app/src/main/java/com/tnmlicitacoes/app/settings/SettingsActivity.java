package com.tnmlicitacoes.app.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.ui.base.BaseActivity;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity {

    private SettingsFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTheme(R.style.SettingsTheme);
        setupToolbar("Configurações");

        mFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settingsFrag");
        if(mFragment == null) {
            mFragment = new SettingsFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_settings, mFragment, "settingsFrag");
            ft.commit();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public String getLogTag() {
        return "SettingsActivity";
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Context mContext;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = context;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mContext = null;
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }

        @Override
        public void onStart() {
            super.onStart();
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            prefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onStop() {
            super.onStop();
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            prefs.unregisterOnSharedPreferenceChangeListener(this);
        }

    }
}