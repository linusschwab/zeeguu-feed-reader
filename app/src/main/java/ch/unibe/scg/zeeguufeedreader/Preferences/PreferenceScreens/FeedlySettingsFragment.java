package ch.unibe.scg.zeeguufeedreader.Preferences.PreferenceScreens;

import android.os.Bundle;

import ch.unibe.scg.zeeguufeedreader.Preferences.BaseSettingsFragment;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedlySettingsFragment extends BaseSettingsFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_feedly);

        //updateAccount();

        //createChangeListener();
    }
}
