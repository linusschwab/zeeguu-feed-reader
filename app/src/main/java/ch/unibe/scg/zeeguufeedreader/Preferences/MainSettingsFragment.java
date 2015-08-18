package ch.unibe.scg.zeeguufeedreader.Preferences;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Settings Fragment to display the different preference screens
 *
 * Workaround to display Actionbar in nested preference screens.
 * See: https://stackoverflow.com/questions/27862299/toolbar-is-hidden-in-nested-preferencescreen
 */
public class MainSettingsFragment extends BaseSettingsFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey() == null)
            return false;

        if (preference.getKey().equals(getString(R.string.pref_zeeguu)))
            settingsCallback.displayPreferenceScreen(getString(R.string.pref_zeeguu));
        else if (preference.getKey().equals(getString(R.string.pref_feedly)))
            settingsCallback.displayPreferenceScreen(getString(R.string.pref_feedly));

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


}