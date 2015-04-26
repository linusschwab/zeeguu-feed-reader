package ch.unibe.scg.zeeguufeedreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        updateAccountSummary();
        createChangeListener();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        MainActivity main = (MainActivity) getActivity();

        // Open Zeeguu login dialog
        if (preference.getKey().equals("pref_zeeguu_account")) {
            main.showZeeguuLoginDialog(getString(R.string.login_zeeguu_title));
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void createChangeListener() {
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        updateAccountSummary();
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    private void updateAccountSummary() {
        Preference zeeguuAccount = findPreference("pref_zeeguu_account");
        String text = sharedPref.getString("pref_zeeguu_username", "");
        if (!text.equals("")) zeeguuAccount.setSummary("Logged in as " + text);
    }
}