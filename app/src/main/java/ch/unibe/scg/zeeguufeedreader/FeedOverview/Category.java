package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;

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

    /*
     Only for read access, feeds stored in this list do not get saved in the database!
     (Workaround because ormlite does not directly support m:m relations)
     */
    private ArrayList<Feed> feeds = new ArrayList<>();

    private int unreadCount;

    public Category() {
        // Empty constructor needed by ormlite
    }

    public Category(String name) {
        this.name = name;
    }

    View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        CategoryViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category, parent, false);
            holder = new CategoryViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.category_icon);
            holder.name = (TextView) convertView.findViewById(R.id.category_name);
            holder.unread = (TextView) convertView.findViewById(R.id.category_unread_count);

            convertView.setTag(holder);
        }
        else {
            holder = (CategoryViewHolder) convertView.getTag();
        }

        // Expanded/Collapsed icon
        if (isExpanded)
            holder.icon.setImageResource(R.drawable.ic_action_collapse);
        else
            holder.icon.setImageResource(R.drawable.ic_action_expand);

        // Name and unread count
        holder.name.setText(name.toUpperCase());
        holder.unread.setText("" + unreadCount);

        return convertView;
    }

    private void calculateUnreadCount() {
        int unreadCounter = 0;

        for (Feed feed : feeds) {
            unreadCounter += feed.getUnreadCount();
        }

        unreadCount = unreadCounter;
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
        calculateUnreadCount();
    }

    public void addFeed(Feed feed) {
        feeds.add(feed);
        calculateUnreadCount();
    }

    public Feed removeFeed(int position) {
        Feed feed = feeds.remove(position);
        calculateUnreadCount();

        return feed;
    }

    public Feed getFeed(int position) {
        return feeds.get(position);
    }

    public int getFeedCount() {
        return feeds.size();
    }

    public int getUnreadCount() {
        calculateUnreadCount();
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
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

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class CategoryViewHolder {
        ImageView icon;
        TextView name;
        TextView unread;
    }
}

