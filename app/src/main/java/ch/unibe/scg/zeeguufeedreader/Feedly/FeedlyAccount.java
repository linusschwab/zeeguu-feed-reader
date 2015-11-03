package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.unibe.scg.zeeguufeedreader.Core.Tools;
import ch.unibe.scg.zeeguufeedreader.Database.CategoryFeed;
import ch.unibe.scg.zeeguufeedreader.Database.QueryHelper;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.DefaultCategory;
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
    private String name;
    private String picture;

    private String authenticationCode;
    private String refreshToken;
    private String accessToken;
    private long accessTokenExpiration;

    private long lastReadSync;
    private long lastFavoriteSync;

    // Subscriptions
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<Feed> feeds = new ArrayList<>();

    // Special Categories
    private DefaultCategory all;
    private DefaultCategory favorite;
    private DefaultCategory recommended;
    private Category uncategorized;

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
        callback.saveString(R.string.pref_feedly_name, name);
        callback.saveString(R.string.pref_feedly_picture, picture);
        callback.saveString(R.string.pref_feedly_authentication_code, authenticationCode);
        callback.saveString(R.string.pref_feedly_refresh_token, refreshToken);
        callback.saveString(R.string.pref_feedly_access_token, accessToken);
        callback.saveLong(R.string.pref_feedly_access_token_expiration, accessTokenExpiration);
    }

    public void load() {
        email = callback.loadString(R.string.pref_feedly_email);
        userId = callback.loadString(R.string.pref_feedly_user_id);
        name = callback.loadString(R.string.pref_feedly_name);
        picture = callback.loadString(R.string.pref_feedly_picture);
        authenticationCode = callback.loadString(R.string.pref_feedly_authentication_code);
        refreshToken = callback.loadString(R.string.pref_feedly_refresh_token);
        accessToken = callback.loadString(R.string.pref_feedly_access_token);
        accessTokenExpiration = callback.loadLong(R.string.pref_feedly_access_token_expiration);
        lastReadSync = callback.loadLong(R.string.pref_feedly_last_read_sync);
        lastFavoriteSync = callback.loadLong(R.string.pref_feedly_last_favorite_sync);
    }

    public void logout() {
        // Delete variables
        email = "";
        userId = "";
        name = "";
        picture = "";
        authenticationCode = "";
        refreshToken = "";
        accessToken = "";
        accessTokenExpiration = 0;

        // TODO: Revoke token

        // Delete shared preferences
        callback.saveString(R.string.pref_feedly_email, "");
        callback.saveString(R.string.pref_feedly_user_id, "");
        callback.saveString(R.string.pref_feedly_name, "");
        callback.saveString(R.string.pref_feedly_picture, "");
        callback.saveString(R.string.pref_feedly_authentication_code, "");
        callback.saveString(R.string.pref_feedly_refresh_token, "");
        callback.saveString(R.string.pref_feedly_access_token, "");
        callback.saveLong(R.string.pref_feedly_access_token_expiration, (long) 0);
        callback.saveLong(R.string.pref_feedly_last_read_sync, (long) 0);
        callback.saveLong(R.string.pref_feedly_last_favorite_sync, (long) 0);
    }

    // Default categories (not saved in the database)
    public void setUpDefaultCategories() {
        all = new DefaultCategory(activity.getResources().getString(R.string.default_category_all));
        favorite = new DefaultCategory(activity.getResources().getString(R.string.default_category_favorite));
        recommended = new DefaultCategory(activity.getResources().getString(R.string.default_category_recommended));

        updateDefaultCategoryEntries();

        all.setEntriesCount(queryHelper.getNumberOfEntries());
        all.setUnreadCount(queryHelper.getNumberOfUnreadEntries());

        favorite.setEntriesCount(queryHelper.getNumberOfFavoriteEntries());
        favorite.setUnreadCount(queryHelper.getNumberOfUnreadFavoriteEntries());

        recommended.setEntriesCount(callback.getArticleRecommender().getEntriesCount());
        recommended.setUnreadCount(callback.getArticleRecommender().getUnreadCount());

        categories.add(0, all);
        categories.add(1, favorite);
        if (callback.getArticleRecommender().isActive())
            categories.add(2, recommended);
    }

    public void updateDefaultCategories() {
        all.setEntriesCount(queryHelper.getNumberOfEntries());
        all.setUnreadCount(queryHelper.getNumberOfUnreadEntries());

        favorite.setEntriesCount(queryHelper.getNumberOfFavoriteEntries());
        favorite.setUnreadCount(queryHelper.getNumberOfUnreadFavoriteEntries());

        recommended.setEntriesCount(callback.getArticleRecommender().getEntriesCount());
        recommended.setUnreadCount(callback.getArticleRecommender().getUnreadCount());

        updateDefaultCategoryEntries();
    }

    public void updateDefaultCategoryEntries() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                all.setEntries(new ArrayList<>(queryHelper.getAllEntries()));
                favorite.setEntries(new ArrayList<>(queryHelper.getFavoriteEntries()));

                callback.getArticleRecommender().setEntries(new ArrayList<>(queryHelper.getRecommendedEntries()));
                recommended.setEntries(callback.getArticleRecommender().getRecommendedEntries());
                recommended.setEntriesCount(callback.getArticleRecommender().getEntriesCount());
                recommended.setUnreadCount(callback.getArticleRecommender().getUnreadCount());
            }
        });

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    // Database Methods
    public void saveCategory(Category category) {
        try {
            if (category.getId() == 0 && !category.equals(uncategorized)) {
                categoryDao.create(category);
                categories.add(category);
            } else
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

        setUpDefaultCategories();

        // Uncategorized
        uncategorized = new Category(activity.getResources().getString(R.string.category_uncategorized));
        categories.add(uncategorized);
    }

    public void synchronizeCategories(ArrayList<Category> categoriesNew) {
        if (categoriesNew.size() == 0)
            return;

        ArrayList<Category> categoriesExisting = getNormalCategories();

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
            if (!categoryExisting.getName().equals(activity.getResources().getString(R.string.category_uncategorized)))
                deleteCategory(categoryExisting);
        }
    }

    public void saveFeed(Feed feed) {
        try {
            if (feed.getId() == 0) {
                feedDao.create(feed);
                feeds.add(feed);
            }
            else
                feedDao.update(feed);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't save feed: " + feed.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void saveFeeds(ArrayList<Feed> feeds) {
        for (Feed feed : feeds) {
            saveFeed(feed);
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

        uncategorized.setFeeds(getFeedsWithoutCategory());
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

                // Add to uncategorized if no categories
                if (categoriesToLink.size() == 0)
                    uncategorized.addFeed(feedNew);
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
            else {
                feedEntryDao.update(entry);
                saveFeed(entry.getFeed());
            }
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't save feed entry: " + entry.getTitle(), e);
            throw new RuntimeException(e);
        }
    }

    public void saveFeedEntries(ArrayList<FeedEntry> entries) {
        for (FeedEntry entry : entries) {
            saveFeedEntry(entry);
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

            // Entry does not exist
            if (entryExisting == null) {
                saveFeedEntry(entryNew);

                // Update feed unread and entry count
                Feed feed = entryNew.getFeed();
                feed.increaseEntriesCount();
                if (!entryNew.isRead())
                    feed.increaseUnreadCount();
            }
        }

        onSynchronizationFinished();
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

        updateDefaultCategories();
        uncategorized.setFeeds(getFeedsWithoutCategory());
    }

    public void onSynchronizationFinished() {
        updateDefaultCategories();
        saveFeeds(feeds);

        // TODO: Move
        if (callback.getArticleRecommender().isActive())
            callback.getArticleRecommender().calculateScoreForNewEntries();
    }

    // Mark as read
    public void markFeedsAsRead(ArrayList<Feed> feeds) {
        for (Feed feed : feeds) {
            ArrayList<FeedEntry> entries = feed.getEntries();
            Date date = new Date(feed.getReadEntriesDate());
            for (FeedEntry entry : entries) {
                // TODO: Make use of sorted entries
                // Entry older than date -> is read
                if (entry.getDate().compareTo(date) < 0) {
                    entry.syncRead(true);
                    saveFeedEntry(entry);
                }
            }
        }
    }

    public void toggleUnreadSwitch(boolean activated) {
        callback.saveBoolean(R.string.pref_feedly_show_unread_only, activated);
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

    public boolean isProfileSet() {
        return !userId.equals("") && (!picture.equals("") || !email.equals("") || !name.equals(""));
    }

    public boolean showUnreadOnly() {
        return callback.loadBoolean(R.string.pref_feedly_show_unread_only, true);
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getPicture() {
        // Decode Base64 image String
        return Tools.byteArrayToBitmap(Base64.decode(picture.getBytes(), Base64.DEFAULT));
    }

    public void setPicture(Bitmap picture) {
        // Encode picture as Base64 string to be able to save in shared preferences
        this.picture = Base64.encodeToString(Tools.bitmapToByteArray(picture), Base64.DEFAULT);
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

    public void readSyncFinished() {
        lastReadSync = System.currentTimeMillis();
        callback.saveLong(R.string.pref_feedly_last_read_sync, lastReadSync);
    }

    public void favoriteSyncFinished() {
        lastFavoriteSync = System.currentTimeMillis();
        callback.saveLong(R.string.pref_feedly_last_favorite_sync, lastFavoriteSync);
    }

    public long getLastReadSync() {
        return lastReadSync;
    }

    public long getLastFavoriteSync() {
        return lastFavoriteSync;
    }

    // Subscriptions
    public ArrayList<Category> getCategories() {
        return categories;
    }

    public ArrayList<DefaultCategory> getDefaultCategories() {
        ArrayList<DefaultCategory> defaultCategories = new ArrayList<>();

        for (Category category : categories) {
            if (category instanceof DefaultCategory)
                defaultCategories.add((DefaultCategory) category);
        }

        return defaultCategories;
    }

    public ArrayList<Category> getNormalCategories() {
        ArrayList<Category> normalCategories = new ArrayList<>();

        for (Category category : categories) {
            if (!(category instanceof DefaultCategory))
                normalCategories.add(category);
        }

        return normalCategories;
    }

    public Category getCategoryById(String feedlyId) {
        for (Category category : categories) {
            if (category.getFeedlyId() != null && category.getFeedlyId().equals(feedlyId))
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

    private ArrayList<Feed> getFeedsWithoutCategory() {
        ArrayList<Feed> feedsWithoutCategory = new ArrayList<>();

        for (Feed feed : feeds)
            if (feed.getCategories().size() == 0)
                feedsWithoutCategory.add(feed);

        return feedsWithoutCategory;
    }

    public FeedEntry getEntryById(String feedlyId) {
        return queryHelper.getFeedEntryByFeedlyId(feedlyId);
    }

    public FeedEntry getEntryById(int id) {
        try {
            return feedEntryDao.queryForId(id);
        }
        catch (SQLException e) {
            Log.e(FeedlyAccount.class.getName(), "Can't get entry with id " + id, e);
        }

        return null;
    }

    public ArrayList<FeedEntry> getAllFeedEntries() {
        return new ArrayList<>(queryHelper.getAllEntries());
    }

    public ArrayList<FeedEntry> getLatestReadEntries(long newerThan) {
        return new ArrayList<>(queryHelper.getLatestReadEntries(newerThan));
    }

    public ArrayList<FeedEntry> getLatestUnreadEntries(long newerThan) {
        return new ArrayList<>(queryHelper.getLatestUnreadEntries(newerThan));
    }

    public ArrayList<FeedEntry> getLatestFavoritedEntries(long newerThan) {
        return new ArrayList<>(queryHelper.getLatestFavoritedEntries(newerThan));
    }

    public ArrayList<FeedEntry> getLatestUnfavoritedEntries(long newerThan) {
        return new ArrayList<>(queryHelper.getLatestUnfavoritedEntries(newerThan));
    }
}