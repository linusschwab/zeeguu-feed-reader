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
            return callback.getDatabaseHelper().getFeedEntryDao().queryForAll();
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get all feed entries", e);
            throw new RuntimeException(e);
        }
    }

    public List<FeedEntry> getFavoriteEntries() {
        try {
            return callback.getDatabaseHelper().getFeedEntryDao().queryForEq("favorite", true);
        }
        catch (SQLException e) {
            Log.e(QueryHelper.class.getName(), "Can't get favorite feed entries", e);
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
}
