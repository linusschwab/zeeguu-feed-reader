package ch.unibe.scg.zeeguufeedreader.Preferences;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;

import ch.unibe.scg.zeeguufeedreader.Core.BaseActivity;
import ch.unibe.scg.zeeguufeedreader.Preferences.PreferenceScreens.FeedlySettingsFragment;
import ch.unibe.scg.zeeguufeedreader.Preferences.PreferenceScreens.ZeeguuSettingsFragment;
import ch.unibe.scg.zeeguufeedreader.R;

public class SettingsActivity extends BaseActivity implements
        SettingsCallbacks {

    private String settingsTitle;

    // Fragments
    private MainSettingsFragment mainSettingsFragment;
    private FeedlySettingsFragment feedlySettingsFragment;
    private ZeeguuSettingsFragment zeeguuSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsTitle = getString(R.string.title_settings);

        createFragments();

        setContentView(R.layout.activity_settings);

        setUpToolbar();
        setUpBackButton();

        switchFragment(mainSettingsFragment, getString(R.string.tag_main_settings), settingsTitle);
    }

    private void createFragments() {
        mainSettingsFragment = (MainSettingsFragment) fragmentManager.findFragmentByTag(getString(R.string.tag_main_settings));
        if (mainSettingsFragment == null) mainSettingsFragment = new MainSettingsFragment();

        feedlySettingsFragment = (FeedlySettingsFragment) fragmentManager.findFragmentByTag(getString(R.string.tag_feedly_settings));
        if (feedlySettingsFragment == null) feedlySettingsFragment = new FeedlySettingsFragment();

        zeeguuSettingsFragment = (ZeeguuSettingsFragment) fragmentManager.findFragmentByTag(getString(R.string.tag_zeeguu_settings));
        if (zeeguuSettingsFragment == null) zeeguuSettingsFragment = new ZeeguuSettingsFragment();
    }

    private void setUpBackButton() {
        ActionBar actionBar = getSupportActionBar();

        if (toolbar != null && actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            popBackStack();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void displayPreferenceScreen(String key) {
        if (key.equals(getString(R.string.pref_zeeguu)))
            switchFragmentBackstack(zeeguuSettingsFragment, getString(R.string.tag_zeeguu_settings), settingsTitle);
        if (key.equals(getString(R.string.pref_feedly)))
            switchFragmentBackstack(feedlySettingsFragment, getString(R.string.tag_feedly_settings), settingsTitle);
    }
}