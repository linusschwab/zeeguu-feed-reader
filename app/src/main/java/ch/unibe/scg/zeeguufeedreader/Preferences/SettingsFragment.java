package ch.unibe.scg.zeeguufeedreader.Preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.R;

public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences sharedPref;
    private SettingsCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface SettingsCallbacks {
        void showZeeguuLoginDialog(String title, String email);
        void showZeeguuLogoutDialog();
    }

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
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (SettingsCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SettingsCallbacks");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Open Zeeguu login dialog
        if (preference.getKey().equals("pref_zeeguu_account")) {
            if (sharedPref.getString("pref_zeeguu_email", "").equals(""))
                callback.showZeeguuLoginDialog("", "");
            else
                callback.showZeeguuLogoutDialog();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void createChangeListener() {
        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if (key.equals("pref_zeeguu_email"))
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
        String text = sharedPref.getString("pref_zeeguu_email", "");
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