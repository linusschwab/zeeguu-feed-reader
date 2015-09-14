package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.R;

@DatabaseTable(tableName = "feeds")
public class Feed {

    // Id is generated by the database and set on the object
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "feedly_id")
    private String feedlyId;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "url")
    private String url;

    @DatabaseField(columnName = "image_url")
    private String imageUrl;

    @DatabaseField(columnName = "color")
    private int color;

    /*
     If eager is set to false then the collection is considered to be "lazy" and will iterate
     over the database using the Dao.iterator() only when a method is called on the collection.
    */
    @ForeignCollectionField(eager = false)
    private ForeignCollection<FeedEntry> entries;

    /*
     Only for read access, categories stored in this list do not get saved in the database!
     (Workaround because ormlite does not directly support m:m relations)
     */
    private ArrayList<Category> categories = new ArrayList<>();

    /*
     List of new categories that are linked during feed synchronization
     */
    private ArrayList<Category> categoriesToLink = new ArrayList<>();

    private int unreadCount;

    public Feed() {
        // Empty constructor needed by ormlite
    }

    public Feed(String name) {
        this.name = name;
    }

    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        FeedViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed, null);
            holder = new FeedViewHolder();

            holder.favicon = (ImageView) convertView.findViewById(R.id.feed_favicon);
            holder.name = (TextView) convertView.findViewById(R.id.feed_name);
            holder.unread = (TextView) convertView.findViewById(R.id.feed_unread_count);

            convertView.setTag(holder);
        }
        else {
            holder = (FeedViewHolder) convertView.getTag();
        }

        holder.name.setText(name);
        holder.unread.setText("" + unreadCount);

        return convertView;
    }

    private int calculateUnreadCount() {
        unreadCount = 0;

        if (entries != null) {
            for (FeedEntry entry : entries) {
                if (entry != null && entry.isUnread())
                    unreadCount++;
            }
        }

        return unreadCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public ArrayList<FeedEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public ArrayList<Category> getCategoriesToLink() {
        return categoriesToLink;
    }

    public void addCategoryToLink(Category category) {
        categoriesToLink.add(category);
    }

    public String getFeedlyId() {
        return feedlyId;
    }

    public void setFeedlyId(String feedlyId) {
        this.feedlyId = feedlyId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getUnreadCount() {
        calculateUnreadCount();
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class FeedViewHolder {
        ImageView favicon;
        TextView name;
        TextView unread;
    }
}
