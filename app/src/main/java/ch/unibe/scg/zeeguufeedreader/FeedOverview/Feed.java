package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.R;

public class Feed {

    private final String name;
    private final long id;
    private String color;

    private ArrayList<FeedEntry> entries = new ArrayList<>();
    private int unreadCount;

    public Feed(String name, long id) {
        this.name = name;
        this.id = id;
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
        for (FeedEntry entry:entries) {
            if (entry.isUnread())
                unreadCount++;
        }

        return unreadCount;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void addEntry(FeedEntry entry) {
        entries.add(entry);
        calculateUnreadCount();
    }

    public ArrayList<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<FeedEntry> entries) {
        this.entries = entries;
        calculateUnreadCount();
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getUnreadCount() {
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
