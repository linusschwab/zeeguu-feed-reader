package ch.unibe.scg.zeeguufeedreader.Feedly;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.Database.DatabaseHelper;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;

/**
 * Callback interface that must be implemented by the container activity
 */
public interface FeedlyCallbacks {
    void displayMessage(String message);
    void setSubscriptions(ArrayList<Category> categories, boolean update);

    void setActionBar(boolean displayBackButton, int actionBarColor);
    void resetActionBar();

    // Authentication
    void displayFeedlyAuthentication(String url);
    void feedlyAuthenticationResponse(String response, boolean successful);

    // Database
    DatabaseHelper getDatabaseHelper();

    // Shared preference helper methods
    void saveString(int prefKey, String value);
    String loadString(int prefKey);
    void saveLong(int prefKey, Long value);
    Long loadLong(int prefKey);
    void saveBoolean(int prefKey, boolean value);
    boolean loadBoolean(int prefKey, boolean defaultValue);
}
