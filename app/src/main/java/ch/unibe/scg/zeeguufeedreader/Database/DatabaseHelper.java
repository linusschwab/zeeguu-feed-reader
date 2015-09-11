package ch.unibe.scg.zeeguufeedreader.Database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 *
 * Source: https://github.com/j256/ormlite-examples/blob/master/android/HelloAndroidNoBase/src/com/example/hellonobase/DatabaseHelper.java
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    // Name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "feeddata.db";
    // Any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 1;

    // The DAO objects we use to access the tables
    private Dao<Category, Integer> categoryDao = null;
    private Dao<CategoryFeed, Integer> categoryFeedDao = null;
    private Dao<Feed, Integer> feedDao = null;
    private Dao<FeedEntry, Integer> feedEntryDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.d(DatabaseHelper.class.getName(), "onCreate");

            // Create the tables
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, CategoryFeed.class);
            TableUtils.createTable(connectionSource, Feed.class);
            TableUtils.createTable(connectionSource, FeedEntry.class);

        }
        catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.d(DatabaseHelper.class.getName(), "onUpgrade");

            // Drop the old tables
            TableUtils.dropTable(connectionSource, Category.class, true);
            TableUtils.dropTable(connectionSource, CategoryFeed.class, true);
            TableUtils.dropTable(connectionSource, Feed.class, true);
            TableUtils.dropTable(connectionSource, FeedEntry.class, true);

            // After we drop the old databases, we create the new ones
            onCreate(db, connectionSource);

        }
        catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Objects (DAO) for our persisted classes. It will create it or just give the cached
     * value.
     */
    public Dao<Category, Integer> getCategoryDao() throws SQLException {
        if (categoryDao == null) {
            categoryDao = getDao(Category.class);
        }
        return categoryDao;
    }

    public Dao<CategoryFeed, Integer> getCategoryFeedDao() throws SQLException {
        if (categoryFeedDao == null) {
            categoryFeedDao = getDao(CategoryFeed.class);
        }
        return categoryFeedDao;
    }

    public Dao<Feed, Integer> getFeedDao() throws SQLException {
        if (feedDao == null) {
            feedDao = getDao(Feed.class);
        }
        return feedDao;
    }

    public Dao<FeedEntry, Integer> getFeedEntryDao() throws SQLException {
        if (feedEntryDao == null) {
            feedEntryDao = getDao(FeedEntry.class);
        }
        return feedEntryDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();

        categoryDao = null;
        feedDao = null;
        feedEntryDao = null;
    }
}