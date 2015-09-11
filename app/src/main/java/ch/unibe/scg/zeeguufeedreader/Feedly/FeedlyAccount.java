package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.Database.CategoryFeed;
import ch.unibe.scg.zeeguufeedreader.Database.DatabaseHelper;
import ch.unibe.scg.zeeguufeedreader.Database.QueryHelper;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Responsible for managing the user data
 */
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
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Feed> feeds = new ArrayList<>();

    // Database access objects
    private Dao<Category, Integer> categoryDao;
    private Dao<CategoryFeed, Integer> categoryFeedDao;
    private Dao<Feed, Integer> feedDao;
    private Dao<FeedEntry, Integer> feedEntryDao;
    private QueryHelper queryHelper;

    public FeedlyAccount(Activity activity) {
        this.activity = activity;
        queryHelper = new QueryHelper(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedlyCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedlyCallbacks");
        }

        getDatabaseAccessObjects();
    }

    public void onRestore(Activity activity) {
        this.activity = activity;
        queryHelper.onRestore(activity);
        callback = (FeedlyCallbacks) activity;
        getDatabaseAccessObjects();
    }

    private void getDatabaseAccessObjects() {
        try {
            categoryDao = callback.getDatabaseHelper().getCategoryDao();
            categoryFeedDao = callback.getDatabaseHelper().getCategoryFeedDao();
            feedDao = callback.getDatabaseHelper().getFeedDao();
            feedEntryDao = callback.getDatabaseHelper().getFeedEntryDao();
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't load DAOs", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Save login information in preferences
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

    // Database Methods
    public void saveCategory(Category category) {
        try {
            if (category.getId() == 0)
                categoryDao.create(category);
            else
                categoryDao.update(category);

            categories.add(category);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't save category: " + category.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void loadCategories() {
        try {
            categories = new ArrayList<>(categoryDao.queryForAll());

            for (Category category : categories)
                category.setFeeds(new ArrayList<>(queryHelper.getFeedsForCategory(category)));
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't load categories", e);
            throw new RuntimeException(e);
        }
    }

    public void saveFeed(Feed feed) {
        try {
            if (feed.getId() == 0)
                feedDao.create(feed);
            else
                feedDao.update(feed);

            feeds.add(feed);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't save feed: " + feed.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void loadFeeds() {
        try {
            feeds = new ArrayList<>(feedDao.queryForAll());

            for (Feed feed : feeds)
                feed.setCategories(new ArrayList<>(queryHelper.getCategoriesForFeed(feed)));
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't load feeds", e);
            throw new RuntimeException(e);
        }
    }

    public void saveFeedEntry(FeedEntry entry) {
        try {
            if (entry.getId() == 0)
                feedEntryDao.create(entry);
            else
                feedEntryDao.update(entry);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't save feed entry: " + entry.getTitle(), e);
            throw new RuntimeException(e);
        }
    }

    public void linkCategoryFeed(Category category, Feed feed) {
        CategoryFeed categoryFeed = new CategoryFeed(category, feed);

        try {
            categoryFeedDao.create(categoryFeed);
            category.addFeed(feed);
            feed.addCategory(category);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't link category " + category.getName() + " with feed " + feed.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void unlinkCategoryFeed(Category category, Feed feed) {
        // TODO: Delete link
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

    public Feed getFeedById(String feedlyId) {
        for (Feed feed : feeds) {
            if (feed.getFeedlyId().equals(feedlyId))
                return feed;
        }
        return null;
    }
}