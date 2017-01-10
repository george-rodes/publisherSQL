package br.com.anagnostou.publisher;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import br.com.anagnostou.publisher.utils.L;

public class AppPreferences extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.preferences_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.add(R.id.preferences_container, settingsFragment, "SETTINGS_FRAGMENT");
        fragmentTransaction.commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        public static final String SOURCE_DATA_IMPORT = "sourceDataImport";

        @Override
        public void onResume() {
            super.onResume();
            // Set up a listener whenever a key changes
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            // Set up a listener whenever a key changes
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            ListPreference lp = (ListPreference) findPreference(SOURCE_DATA_IMPORT);
            lp.setSummary("dummy"); // required or will not update
            lp.setSummary(getString(R.string.os_registros_serao_obtidos));
        }
    }
}
