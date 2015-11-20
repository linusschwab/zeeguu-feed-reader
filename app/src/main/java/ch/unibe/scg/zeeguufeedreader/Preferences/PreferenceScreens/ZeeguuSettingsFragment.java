package ch.unibe.scg.zeeguufeedreader.Preferences.PreferenceScreens;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import ch.unibe.scg.zeeguufeedreader.Preferences.BaseSettingsFragment;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

public class ZeeguuSettingsFragment extends BaseSettingsFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private ZeeguuSettingsCallbacks callback;

    private Preference zeeguuAccount;
    private Preference languageNative;
    private Preference languageLearning;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface ZeeguuSettingsCallbacks {
        ZeeguuConnectionManager getZeeguuConnectionManager();
        void showZeeguuLoginDialog(String title, String email);
        void showZeeguuLogoutDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_zeeguu);

        zeeguuAccount = findPreference("pref_zeeguu_account");
        languageNative = findPreference("pref_zeeguu_language_native");
        languageLearning = findPreference("pref_zeeguu_language_learning");

        updateNativeLanguage();
        updateLearningLanguage();
        updateAccount();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ZeeguuSettingsCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement ZeeguuSettingsCallbacks");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // Open Zeeguu login dialog
        if (preference.getKey() != null && preference.getKey().equals("pref_zeeguu_account")) {
            if (sharedPref.getString("pref_zeeguu_email", "").equals(""))
                callback.showZeeguuLoginDialog("", "");
            else
                callback.showZeeguuLogoutDialog();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_zeeguu_email"))
            updateAccount();
        else if (key.equals("pref_zeeguu_language_native"))
            updateNativeLanguage();
        else if (key.equals("pref_zeeguu_language_learning"))
            updateLearningLanguage();
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    private void updateAccount() {
        String text = sharedPref.getString("pref_zeeguu_email", "");
        if (!text.equals("") && isAdded()) {
            zeeguuAccount.setSummary(getString(R.string.settings_zeguu_account_login) + " " + text);
            enableLanguages();
        }
        else {
            zeeguuAccount.setSummary(R.string.settings_zeguu_account_summary);
            disableLanguages();
        }
    }

    private void enableLanguages() {
        languageNative.setEnabled(true);
        updateNativeLanguage();
        languageLearning.setEnabled(true);
        updateLearningLanguage();
    }

    private void disableLanguages() {
        languageNative.setEnabled(false);
        languageNative.setSummary(R.string.settings_zeguu_language_native_summary);
        languageLearning.setEnabled(false);
        languageLearning.setSummary(R.string.settings_zeguu_language_learning_summary);
    }

    /**
     * Updates the summary and sets the native language on the server (if it changed)
     */
    private void updateNativeLanguage() {
        String language = sharedPref.getString("pref_zeeguu_language_native", "");
        if (!language.equals("") && isAdded()) {
            callback.getZeeguuConnectionManager().setLanguageNative(language);
            languageNative.setSummary(language);
        }
    }

    /**
     * Updates the summary and sets the learning language on the server (if it changed)
     */
    private void updateLearningLanguage() {
        String language = sharedPref.getString("pref_zeeguu_language_learning", "");
        if (!language.equals("") && isAdded()) {
            callback.getZeeguuConnectionManager().setLanguageLearning(language);
            languageLearning.setSummary(language);
        }
    }
}
