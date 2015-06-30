package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.R;

public class Feed {

    private final String name;
    private final long id;

    public Feed(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed, null);
            holder = new ViewHolder();

            holder.favicon = (ImageView) convertView.findViewById(R.id.feed_favicon);
            holder.name = (TextView) convertView.findViewById(R.id.feed_name);
            holder.unread = (TextView) convertView.findViewById(R.id.feed_unread_count);

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

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        ImageView favicon;
        TextView name;
        TextView unread;
    }
}
