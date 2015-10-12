package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.Core.ContextManager;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Special categories that only have entries, not saved in the database
 */
public class DefaultCategory extends Category {

    public DefaultCategory(String name) {
        super(name);
    }

    @Override
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

        // Set list icon
        holder.icon.setImageDrawable(ContextCompat.getDrawable(ContextManager.getContext(), R.drawable.ic_list));
        holder.border.setVisibility(View.INVISIBLE);

        // Name and unread count
        holder.name.setText(getName());

        if (unread)
            holder.unread.setText("" + unreadCount);
        else
            holder.unread.setText("" + entriesCount);

        return convertView;
    }

    // Getter/Setter
    public void setEntries(ArrayList<FeedEntry> entries) {
        this.entries = entries;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getEntriesCount() {
        return entriesCount;
    }

    public void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }
}
