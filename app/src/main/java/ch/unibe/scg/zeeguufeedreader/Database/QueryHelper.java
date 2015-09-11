package ch.unibe.scg.zeeguufeedreader.Database;

import android.app.Activity;

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
