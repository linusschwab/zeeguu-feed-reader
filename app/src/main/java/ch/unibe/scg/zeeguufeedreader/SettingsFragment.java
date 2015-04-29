package ch.unibe.scg.zeeguufeedreader;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        updateAccount();
        updateNativeLanguageSummary();
        updateLearningLanguageSummary();
        createChangeListener();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        MainActivity main = (MainActivity) getActivity();

        // Open Zeeguu login dialog
        if (preference.getKey().equals("pref_zeeguu_account")) {
            if (sharedPref.getString("pref_zeeguu_username", "").equals(""))
                main.getConnectionManager().showLoginDialog(getString(R.string.login_zeeguu_title));
            else
                main.getConnectionManager().showLoginDialog(getString(R.string.logout_zeeguu_title));
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void createChangeListener() {
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (key.equals("pref_zeeguu_username"))
                            updateAccount();
                        else if (key.equals("pref_zeeguu_language_native"))
                            updateNativeLanguageSummary();
                        else if (key.equals("pref_zeeguu_language_learning"))
                            updateLearningLanguageSummary();
                    }
                };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);
    }

    private void updateAccount() {
        Preference zeeguuAccount = findPreference("pref_zeeguu_account");
        Preference languageNative = findPreference("pref_zeeguu_language_native");
        Preference languageLearning = findPreference("pref_zeeguu_language_learning");
        String text = sharedPref.getString("pref_zeeguu_username", "");
        if (!text.equals("") && isAdded()) {
            zeeguuAccount.setSummary(getString(R.string.settings_zeguu_account_login) + " " + text);
            languageNative.setEnabled(true);
            languageLearning.setEnabled(true);
        }
        else {
            zeeguuAccount.setSummary(R.string.settings_zeguu_account_summary);
            languageNative.setEnabled(false);
            languageLearning.setEnabled(false);
        }
    }

    private void updateNativeLanguageSummary() {
        Preference languageNative = findPreference("pref_zeeguu_language_native");
        String text = sharedPref.getString("pref_zeeguu_language_native", "");
        if (!text.equals("") && isAdded())
            languageNative.setSummary(text);
        else
            languageNative.setSummary(R.string.settings_zeguu_language_native_summary);
    }

    private void updateLearningLanguageSummary() {
        Preference languageLearning = findPreference("pref_zeeguu_language_learning");
        String text = sharedPref.getString("pref_zeeguu_language_learning", "");
        if (!text.equals("") && isAdded())
            languageLearning.setSummary(text);
        else
            languageLearning.setSummary(R.string.settings_zeguu_language_learning_summary);
    }
}