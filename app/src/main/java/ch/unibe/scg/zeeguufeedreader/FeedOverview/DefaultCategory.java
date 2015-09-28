package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import ch.unibe.scg.zeeguufeedreader.Core.ContextManager;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Special categories that only have entries, not saved in the database
 */
public class DefaultCategory extends Category {

    private ArrayList<FeedEntry> entries = new ArrayList<>();

    private int unreadCount;

    public DefaultCategory(String name) {
        super(name);
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

        // Set list icon

        holder.icon.setImageDrawable(ContextCompat.getDrawable(ContextManager.getContext(), R.drawable.ic_list));

        // Name and unread count
        holder.name.setText(getName().toUpperCase());
        holder.unread.setText("" + unreadCount);

        return convertView;
    }

    private void calculateUnreadCount() {
        int unreadCounter = 0;

        for (FeedEntry entry : entries) {
            if (!entry.isRead())
                unreadCounter++;
        }

        unreadCount = unreadCounter;
    }

    // Getter/Setter
    public ArrayList<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<FeedEntry> entries) {
        Collections.sort(entries);
        this.entries = entries;
    }

    public int getUnreadCount() {
        calculateUnreadCount();
        return unreadCount;
    }
}
