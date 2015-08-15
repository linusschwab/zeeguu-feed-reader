package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedlyAccount {

    private Activity activity;
    private FeedlyCallbacks callback;

    // User Information
    private String email;
    private String userId;

    private String authenticationCode;
    private String refreshToken;
    private String accessToken;
    private long accessTokenExpiration;

    // Subscriptions
    private ArrayList<Category> categories;
    private ArrayList<Feed> feeds;

    public FeedlyAccount(Activity activity) {
        this.activity = activity;

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedlyCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedlyCallbacks");
        }
    }

    public void onRestore(Activity activity) {
        this.activity = activity;
        callback = (FeedlyCallbacks) activity;
    }

    /**
     * Save login information in preferences if they are correct (if server sent sessionID)
     */
    public void saveLoginInformation() {
        callback.saveString(R.string.pref_feedly_email, email);
        callback.saveString(R.string.pref_feedly_user_id, userId);
        callback.saveString(R.string.pref_feedly_authentication_code, authenticationCode);
        callback.saveString(R.string.pref_feedly_refresh_token, refreshToken);
        callback.saveString(R.string.pref_feedly_access_token, accessToken);
        callback.saveLong(R.string.pref_feedly_access_token_expiration, accessTokenExpiration);
    }

    public void load() {
        email = callback.loadString(R.string.pref_feedly_email);
        userId = callback.loadString(R.string.pref_feedly_user_id);
        authenticationCode = callback.loadString(R.string.pref_feedly_authentication_code);
        refreshToken = callback.loadString(R.string.pref_feedly_refresh_token);
        accessToken = callback.loadString(R.string.pref_feedly_access_token);
        accessTokenExpiration = callback.loadLong(R.string.pref_feedly_access_token_expiration);
    }

    public void logout() {
        // Delete variables
        email = "";
        userId = "";
        authenticationCode = "";
        refreshToken = "";
        accessToken = "";
        accessTokenExpiration = 0;

        // TODO: Revoke token

        // Delete shared preferences
        callback.saveString(R.string.pref_feedly_email, "");
        callback.saveString(R.string.pref_feedly_user_id, "");
        callback.saveString(R.string.pref_feedly_authentication_code, "");
        callback.saveString(R.string.pref_feedly_refresh_token, "");
        callback.saveString(R.string.pref_feedly_access_token, "");
        callback.saveLong(R.string.pref_feedly_access_token_expiration, (long) 0);
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isUserLoggedIn() {
        return !(authenticationCode == null || authenticationCode.equals("")) || isUserInSession();
    }

    public boolean isUserInSession() {
        return !(refreshToken == null || refreshToken.equals(""));
    }

    public boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isAccessTokenExpired() {
        long expirationTime = accessTokenExpiration;
        long currentTime = System.currentTimeMillis() / 1000;

        return currentTime > expirationTime;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;

        // Delete authentication code (valid only once)
        authenticationCode = "";
        callback.saveString(R.string.pref_feedly_authentication_code, "");
    }

    public String getAccessToken() {
        if (!isAccessTokenExpired())
            return accessToken;
        else
            return null;
    }

    public void setAccessToken(String accessToken, String accessTokenExpiration) {
        this.accessToken = accessToken;
        this.accessTokenExpiration = Long.parseLong(accessTokenExpiration);
    }

    // Subscriptions
    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        this.categories.add(category);
    }

    public Category getCategoryById(String feedlyId) {
        for (Category category : categories) {
            if (category.getFeedlyId().equals(feedlyId))
                return category;
        }
        return null;
    }

    public ArrayList<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(ArrayList<Feed> feeds) {
        this.feeds = feeds;
    }

    public void addFeed(Feed feed) {
        this.feeds.add(feed);
    }

    public Feed getFeedById(String feedlyId) {
        for (Feed feed : feeds) {
            if (feed.getFeedlyId().equals(feedlyId))
                return feed;
        }
        return null;
    }
}