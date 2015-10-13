package ch.unibe.scg.zeeguufeedreader.Database;

import android.app.Activity;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;

import java.sql.SQLException;
import java.util.List;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;

/**
 * Database class with predefined queries
 */
public class QueryHelper {

    private DatabaseCallbacks callback;

    private PreparedQuery<Category> categoriesForFeedQuery;
    private PreparedQuery<Feed> feedsForCategoryQuery;

    public QueryHelper(Activity activity) {
        // Make sure that the interface is implemented in the container activity
        try {
            callback = (DatabaseCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DatabaseCallbacks");
        }
    }

    public void onRestore(Activity activity) {
        callback = (DatabaseCallbacks) activity;
    }

    // Query methods
    public List<Category> getCategoriesForFeed(Feed feed) throws SQLException {
        if (categoriesForFeedQuery == null)
            categoriesForFeedQuery = buildCategoriesForFeedQuery();

        categoriesForFeedQuery.setArgumentHolderValue(0, feed);

        Dao<Category, Integer> categoryDao = callback.getDatabaseHelper().getCategoryDao();
        return categoryDao.query(categoriesForFeedQuery);
    }

    public List<Feed> getFeedsForCategory(Category category) throws SQLException {
        if (feedsForCategoryQuery == null)
            feedsForCategoryQuery = buildFeedsForCategoryQuery();

        feedsForCategoryQuery.setArgumentHolderValue(0, category);

        Dao<Feed, Integer> feedDao = callback.getDatabaseHelper().getFeedDao();
        return feedDao.query(feedsForCategoryQuery);
    }

    public List<FeedEntry> getAllEntries() {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().queryBuilder()
                    .orderBy("date", false).query();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get all feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfEntries() {
        try {
            return (int) callback.getDatabaseHelper().getFeedEntryDao().countOf();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get number of feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfUnreadEntries() {
        try {
            return (int) callback.getDatabaseHelper().getFeedEntryDao().queryBuilder()
                    .where().eq("read", false).countOf();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get number of unread feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public List<FeedEntry> getFavoriteEntries() {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().queryBuilder()
                    .orderBy("date", false).where().eq("favorite", true).query();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get favorite feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfFavoriteEntries() {
        try {
            return (int) callback.getDatabaseHelper().getFeedEntryDao().queryBuilder()
                    .where().eq("favorite", true).countOf();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get number of feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public int getNumberOfUnreadFavoriteEntries() {
        try {
            return (int) callback.getDatabaseHelper().getFeedEntryDao().queryBuilder()
                    .where().eq("favorite", true).and().eq("read", false).countOf();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get number of unread feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public List<FeedEntry> getLatestReadEntries(long newerThan) {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().query(buildLatestReadEntriesQuery(newerThan, true));
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get latest read feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public List<FeedEntry> getLatestUnreadEntries(long newerThan) {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().query(buildLatestReadEntriesQuery(newerThan, false));
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get latest unread feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public List<FeedEntry> getLatestFavoritedEntries(long newerThan) {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().query(buildLatestFavoriteEntriesQuery(newerThan, true));
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get latest favorited feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public List<FeedEntry> getLatestUnfavoritedEntries(long newerThan) {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().query(buildLatestFavoriteEntriesQuery(newerThan, false));
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get latest unfavorited feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public Category getCategoryByFeedlyId(String feedlyId) {
        try {
            Dao<Category, Integer> categoryDao = callback.getDatabaseHelper().getCategoryDao();
            List<Category> result = categoryDao.queryForEq("feedly_id", feedlyId);

            if (result.size() != 0)
                return result.get(0);
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get category by feedly id", e);
            throw new RuntimeException(e);
        }

        return null;
    }

    public Feed getFeedByFeedlyId(String feedlyId) {
        try {
            Dao<Feed, Integer> feedDao = callback.getDatabaseHelper().getFeedDao();
            List<Feed> result = feedDao.queryForEq("feedly_id", feedlyId);

            if (result.size() != 0)
                return result.get(0);
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get feed by feedly id", e);
            throw new RuntimeException(e);
        }

        return null;
    }

    public FeedEntry getFeedEntryByFeedlyId(String feedlyId) {
        try {
            Dao<FeedEntry, Integer> feedEntryDao = callback.getDatabaseHelper().getFeedEntryDao();
            List<FeedEntry> result = feedEntryDao.queryForEq("feedly_id", feedlyId);

            if (result.size() != 0)
                return result.get(0);
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get entry by feedly id", e);
            throw new RuntimeException(e);
        }

        return null;
    }

    // Query builder methods
    private PreparedQuery<Category> buildCategoriesForFeedQuery() throws SQLException {
        // Build inner query for CategoryFeed objects
        Dao<CategoryFeed, Integer> categoryFeedDao = callback.getDatabaseHelper().getCategoryFeedDao();
        QueryBuilder<CategoryFeed, Integer> categoryFeedQueryBuilder = categoryFeedDao.queryBuilder();

        // Select the category_id field
        categoryFeedQueryBuilder.selectColumns("category_id");
        SelectArg feedSelectArg = new SelectArg();
        categoryFeedQueryBuilder.where().eq("feed_id", feedSelectArg);

        // Build outer query for Category objects
        Dao<Category, Integer> categoryDao = callback.getDatabaseHelper().getCategoryDao();
        QueryBuilder<Category, Integer> categoryQueryBuilder = categoryDao.queryBuilder();

        // Where the id matches the category_id from the inner query
        categoryQueryBuilder.where().in("id", categoryFeedQueryBuilder);
        return categoryQueryBuilder.prepare();
    }

    private PreparedQuery<Feed> buildFeedsForCategoryQuery() throws SQLException {
        // Build inner query for CategoryFeed objects
        Dao<CategoryFeed, Integer> categoryFeedDao = callback.getDatabaseHelper().getCategoryFeedDao();
        QueryBuilder<CategoryFeed, Integer> categoryFeedQueryBuilder = categoryFeedDao.queryBuilder();

        // Select the feed_id field
        categoryFeedQueryBuilder.selectColumns("feed_id");
        SelectArg categorySelectArg = new SelectArg();
        categoryFeedQueryBuilder.where().eq("category_id", categorySelectArg);

        // Build outer query for Feed objects
        Dao<Feed, Integer> feedDao = callback.getDatabaseHelper().getFeedDao();
        QueryBuilder<Feed, Integer> feedQueryBuilder = feedDao.queryBuilder();

        // Where the id matches the feed_id from the inner query
        feedQueryBuilder.where().in("id", categoryFeedQueryBuilder);
        return feedQueryBuilder.prepare();
    }

    private PreparedQuery<FeedEntry> buildLatestReadEntriesQuery(long newerThan, boolean read) throws SQLException {
        Dao<FeedEntry, Integer> feedEntryDao = callback.getDatabaseHelper().getFeedEntryDao();
        QueryBuilder<FeedEntry, Integer> feedEntryQueryBuilder = feedEntryDao.queryBuilder();

        feedEntryQueryBuilder.where().between("read_update", newerThan, System.currentTimeMillis()).and().eq("read", read);
        return feedEntryQueryBuilder.prepare();
    }

    private PreparedQuery<FeedEntry> buildLatestFavoriteEntriesQuery(long newerThan, boolean favorite) throws SQLException {
        Dao<FeedEntry, Integer> feedEntryDao = callback.getDatabaseHelper().getFeedEntryDao();
        QueryBuilder<FeedEntry, Integer> feedEntryQueryBuilder = feedEntryDao.queryBuilder();

        feedEntryQueryBuilder.where().between("favorite_update", newerThan, System.currentTimeMillis()).and().eq("favorite", favorite);
        return feedEntryQueryBuilder.prepare();
    }
}
