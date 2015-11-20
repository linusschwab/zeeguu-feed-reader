package ch.unibe.scg.zeeguufeedreader.Preferences.PreferenceScreens;

import android.os.Bundle;
import android.preference.Preference;

import ch.unibe.scg.zeeguufeedreader.Preferences.BaseSettingsFragment;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedlySettingsFragment extends BaseSettingsFragment {

    private Preference feedlyAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_feedly);

        feedlyAccount = findPreference(getString(R.string.pref_feedly_account));

        updateAccount();
    }

    private void updateAccount() {
        String email = sharedPref.getString(getString(R.string.pref_feedly_email), "");
        String name = sharedPref.getString(getString(R.string.pref_feedly_name), "");
        String id = sharedPref.getString(getString(R.string.pref_feedly_user_id), "");

        if (!email.equals("") && isAdded())
            feedlyAccount.setSummary(getString(R.string.settings_feedly_account_login) + " " + email);
        else if (!name.equals("") && isAdded())
            feedlyAccount.setSummary(getString(R.string.settings_feedly_account_login) + " " + name);
        else if (!id.equals("") && isAdded())
            feedlyAccount.setSummary(getString(R.string.settings_feedly_account_logged));
        else
            feedlyAccount.setSummary(R.string.settings_feedly_account_summary);
    }
}
