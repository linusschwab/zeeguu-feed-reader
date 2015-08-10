package ch.unibe.scg.zeeguufeedreader.Feedly;

/**
 * Callback interface that must be implemented by the container activity
 */
public interface FeedlyCallbacks {
    void displayFeedlyAuthentication(String url);
    void displayMessage(String message);

    // Shared preference helper methods
    void saveString(int prefKey, String value);
    String loadString(int prefKey);
    void saveLong(int prefKey, Long value);
    Long loadLong(int prefKey);
}
