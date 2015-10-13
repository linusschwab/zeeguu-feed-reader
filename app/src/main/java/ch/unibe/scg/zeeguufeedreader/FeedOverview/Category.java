package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collections;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;

@DatabaseTable(tableName = "categories")
public class Category {

    // Id is generated by the database and set on the object
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = "feedly_id")
    private String feedlyId;

    @DatabaseField(columnName = "name")
    private String name;

    @DatabaseField(columnName = "is_expanded")
    private boolean isExpanded;

    private boolean border;

    /*
     Only for read access, feeds stored in this list do not get saved in the database!
     (Workaround because ormlite does not directly support m:m relations)
     */
    private ArrayList<Feed> feeds = new ArrayList<>();
    private ArrayList<Feed> feedsVisible = new ArrayList<>();

    protected ArrayList<FeedEntry> entries = new ArrayList<>();

    /*
     Not saved in database, calculated from feed read/unread count
     */
    protected int unreadCount;
    protected int entriesCount;

    public Category() {
        // Empty constructor needed by ormlite
    }

    public Category(String name) {
        this.name = name;
    }

    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent, boolean unread) {
        CategoryViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category, parent, false);
            holder = new CategoryViewHolder();

            holder.border = (RelativeLayout) convertView.findViewById(R.id.category_border);
            holder.icon = (ImageView) convertView.findViewById(R.id.category_icon);
            holder.name = (TextView) convertView.findViewById(R.id.category_name);
            holder.unread = (TextView) convertView.findViewById(R.id.category_unread_count);

            convertView.setTag(holder);
        }
        else {
            holder = (CategoryViewHolder) convertView.getTag();
        }

        // Expanded/Collapsed icon
        if (isExpanded) {
            holder.icon.setImageResource(R.drawable.ic_action_collapse);
            holder.border.setVisibility(View.VISIBLE);
        }
        else {
            holder.icon.setImageResource(R.drawable.ic_action_expand);

            if (border)
                holder.border.setVisibility(View.VISIBLE);
            else
                holder.border.setVisibility(View.INVISIBLE);
        }

        // Name and unread count
        holder.name.setText(name);

        if (unread)
            holder.unread.setText("" + getUnreadCount());
        else
            holder.unread.setText("" + getEntriesCount());

        return convertView;
    }

    private void calculateUnreadCount() {
        int unreadCounter = 0;

        for (Feed feed : feeds) {
            unreadCounter += feed.getUnreadCount();
        }

        unreadCount = unreadCounter;
    }

    private void calculateEntriesCount() {
        int entriesCounter = 0;

        for (Feed feed : feeds) {
            entriesCounter += feed.getEntriesCount();
        }

        entriesCount = entriesCounter;
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

    public String getFeedlyId() {
        return feedlyId;
    }

    public void setFeedlyId(String feedlyId) {
        this.feedlyId = feedlyId;
    }

    // Feeds
    public void setFeeds(ArrayList<Feed> feeds) {
        this.feeds = feeds;
    }

    public void addFeed(Feed feed) {
        feeds.add(feed);
    }

    public Feed removeFeed(int position) {
        Feed feed = feeds.remove(position);
        return feed;
    }

    public Feed getFeed(int position) {
        return feedsVisible.get(position);
    }

    public int getFeedCount(boolean unread) {
        hideEmptyFeeds(unread);
        return feedsVisible.size();
    }

    private void hideEmptyFeeds(boolean unread) {
        feedsVisible = new ArrayList<>(feeds);

        // Remove empty feeds
        if (unread) {
            for (Feed feed : feeds)
                if (feed.getUnreadCount() == 0)
                    feedsVisible.remove(feed);
        }
        else {
            for (Feed feed : feeds)
                if (feed.getEntriesCount() == 0)
                    feedsVisible.remove(feed);
        }
    }


    private void updateEntries() {
        entries = new ArrayList<>();
        for (Feed feed : feeds)
            entries.addAll(feed.getEntries());

        Collections.sort(entries);
    }

    public ArrayList<FeedEntry> getEntries() {
        updateEntries();
        return entries;
    }

    public ArrayList<FeedEntry> getUnreadEntries() {
        ArrayList<FeedEntry> unreadEntries = new ArrayList<>();

        updateEntries();
        for (FeedEntry entry : entries)
            if (!entry.isRead())
                unreadEntries.add(entry);

        return unreadEntries;
    }

    public int getUnreadCount() {
        calculateUnreadCount();
        return unreadCount;
    }

    public int getEntriesCount() {
        calculateEntriesCount();
        return entriesCount;
    }

    // Expanded
    public boolean isExpanded() {
        return isExpanded;
    }

    public void expand() {
        isExpanded = true;
    }

    public void collapse() {
        isExpanded = false;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (feedlyId != null ? !feedlyId.equals(category.feedlyId) : category.feedlyId != null)
            return false;
        return !(name != null ? !name.equals(category.name) : category.name != null);
    }

    @Override
    public int hashCode() {
        int result = feedlyId != null ? feedlyId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class CategoryViewHolder {
        RelativeLayout border;
        ImageView icon;
        TextView name;
        TextView unread;
    }
}

