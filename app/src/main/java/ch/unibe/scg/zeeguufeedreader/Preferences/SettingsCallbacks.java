package ch.unibe.scg.zeeguufeedreader.Preferences;

import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 *  Callback interface that must be implemented by the container activity
 */
public interface SettingsCallbacks {
    void displayPreferenceScreen(String key);
}
