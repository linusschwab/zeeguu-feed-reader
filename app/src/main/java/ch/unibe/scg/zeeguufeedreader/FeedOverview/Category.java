package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.R;

public class Category {

    private final String name;
    private final long id;
    private String feedlyId;

    private ArrayList<Feed> feeds;
    private int unreadCount;
    private boolean isExpanded;

    public Category(String name, long id) {
        this.name = name;
        this.id = id;
        this.feeds = new ArrayList<Feed>();
    }

    View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        CategoryViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category, null);
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

    private int calculateUnreadCount() {
        unreadCount = 0;

        for (Feed feed : feeds) {
            unreadCount += feed.getUnreadCount();
        }

        return unreadCount;
    }

    public String getName() {
        return name;
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

