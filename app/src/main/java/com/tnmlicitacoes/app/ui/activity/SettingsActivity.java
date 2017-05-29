package com.tnmlicitacoes.app.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.utils.AndroidUtilities;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

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

            Preference changedPref = findPreference(key);
            if (key.equals(SettingsUtils.PREF_KEY_USER_DEFAULT_EMAIL)) {
                String userEmail = SettingsUtils.getUserDefaultEmail(mContext);
                if(AndroidUtilities.isEmailValid(userEmail)) {
                    changedPref.setSummary(userEmail);
                } else {
                    changedPref.setSummary(getString(R.string.email_default_summary));
                    sharedPreferences.edit().putString(key, mContext.getString(R.string.pref_email_default)).apply();
                    Toast.makeText(getContext(), getString(R.string.invalid_email), Toast.LENGTH_SHORT).show();
                }
            }
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
