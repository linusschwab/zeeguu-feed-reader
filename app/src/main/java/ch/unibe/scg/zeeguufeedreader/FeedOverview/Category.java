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

    private ArrayList<Feed> feeds;
    private int unreadCount;
    private boolean isExpanded;

    public Category(String name, long id) {
        this.name = name;
        this.id = id;
        this.feeds = new ArrayList<Feed>();
    }

    View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.category, null);
            holder = new ViewHolder();

            holder.icon = (ImageView) convertView.findViewById(R.id.category_icon);
            holder.name = (TextView) convertView.findViewById(R.id.category_name);
            holder.unread = (TextView) convertView.findViewById(R.id.category_unread_count);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(name);
        // TODO: calculate unread count
        holder.unread.setText("0");

        return convertView;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    // Feeds
    public void addFeed(Feed feed) {
        feeds.add(feed);
    }

    public Feed removeFeed(int position) {
        return feeds.remove(position);
    }

    public Feed getFeed(int position) {
        return feeds.get(position);
    }

    public int getFeedCount() {
        return feeds.size();
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
    static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView unread;
    }
}

