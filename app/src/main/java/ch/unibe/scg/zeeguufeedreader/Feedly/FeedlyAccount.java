package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            if (category.getId() == 0) {
                categoryDao.create(category);
                categories.add(category);
            }
            else
                categoryDao.update(category);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't save category: " + category.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteCategory(Category category) {
        try {
            categoryDao.delete(category);
            categories.remove(category);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't delete category: " + category.getName(), e);
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

    public void synchronizeCategories(ArrayList<Category> categoriesNew) {
        if (categoriesNew.size() == 0)
            return;

        ArrayList<Category> categoriesExisting = new ArrayList<>(categories);

        for (Category categoryNew : categoriesNew) {
            Category categoryExisting = getCategoryById(categoryNew.getFeedlyId());

            // Category exists
            if (categoryExisting != null) {
                // Update name if it changed
                if (!categoryNew.getName().equals(categoryExisting.getName())) {
                    categoryExisting.setName(categoryNew.getName());
                    saveCategory(categoryExisting);
                }

                categoriesExisting.remove(categoryExisting);
            }
            // New category
            else
                saveCategory(categoryNew);
        }

        // Delete categories that don't exist on the server any more
        for (Category categoryExisting : categoriesExisting) {
            deleteCategory(categoryExisting);
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

    public void deleteFeed(Feed feed) {
        try {
            feedDao.delete(feed);
            feeds.remove(feed);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't delete feed: " + feed.getName(), e);
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

    public void synchronizeFeeds(ArrayList<Feed> feedsNew) {
        if (feedsNew.size() == 0)
            return;

        ArrayList<Feed> feedsExisting = new ArrayList<>(feeds);

        for (Feed feedNew : feedsNew) {
            Feed feedExisting = getFeedById(feedNew.getFeedlyId());

            // Feed exists
            if (feedExisting != null) {
                // Update name if it changed
                if (!feedExisting.getName().equals(feedNew.getName())) {
                    feedExisting.setName(feedNew.getName());
                    saveFeed(feedExisting);
                }

                feedsExisting.remove(feedExisting);
            }
            // New feed
            else {
                saveFeed(feedNew);

                // Link feed with categories
                ArrayList<Category> categoriesToLink = feedNew.getCategoriesToLink();
                for (Category category : categoriesToLink)
                    linkCategoryFeed(category, feedNew);
            }
        }

        // Delete feeds that don't exist on the server any more
        for (Feed feedExisting : feedsExisting) {
            deleteFeed(feedExisting);
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

    public void deleteFeedEntry(FeedEntry entry) {
        try {
            feedEntryDao.delete(entry);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't delete feed entry: " + entry.getTitle(), e);
            throw new RuntimeException(e);
        }
    }

    public void synchronizeFeedEntries(ArrayList<FeedEntry> entriesNew) {
        for (FeedEntry entryNew : entriesNew) {
            FeedEntry entryExisting = null;
            try {
                List<FeedEntry> result = feedEntryDao.queryForEq("feedly_id", entryNew.getFeedlyId());

                if (result.size() != 0)
                    entryExisting = result.get(0);
            }
            catch (SQLException e) {
                Log.e(FeedlyAccount.class.getName(), "Query error", e);
                throw new RuntimeException(e);
            }

            // Entry exists
            if (entryExisting == null)
                saveFeedEntry(entryNew);
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

    public void loadCategoryFeed() {
        try {
            for (Category category : categories)
                category.setFeeds(new ArrayList<>(queryHelper.getFeedsForCategory(category)));

            for (Feed feed : feeds)
                feed.setCategories(new ArrayList<>(queryHelper.getCategoriesForFeed(feed)));
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't load category feed link", e);
            throw new RuntimeException(e);
        }
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