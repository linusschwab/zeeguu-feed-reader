package ch.unibe.scg.zeeguufeedreader.Database;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;

/**
 * Database class to allow a many-to-many relation between categories and feeds in ormlite
 */
@DatabaseTable(tableName = "category_feed")
public class CategoryFeed {

    /**
     * This id is generated by the database and set on the object when it is passed to the create method. An id is
     * needed in case we need to update or delete this object in the future (ormlite does not support multiple
     * primary keys).
     */
    @DatabaseField(generatedId = true)
    private int id;

    // This is a foreign object which just stores the id from the Category object in this table.
    @DatabaseField(foreign = true, columnName = "category_id", columnDefinition = "integer references categories(id) on delete cascade")
    private Category category;

    // This is a foreign object which just stores the id from the Feed object in this table.
    @DatabaseField(foreign = true, columnName = "feed_id", columnDefinition = "integer references feeds(id) on delete cascade")
    private Feed feed;

    CategoryFeed() {
        // Empty constructor needed by ormlite
    }

    public CategoryFeed(Category category, Feed feed) {
        this.category = category;
        this.feed = feed;
    }
}
