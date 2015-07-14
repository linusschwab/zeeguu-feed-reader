package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.R;

public class FeedEntry {

    private final String title;
    private final long id;

    private String content;
    private String url;
    private String author;
    private int published;
    private boolean unread;

    public FeedEntry(String title, String content, String url, String author, int published, long id) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.author = author;
        this.published = published;
        this.id = id;
    }

    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed_entry, null);
            holder = new ViewHolder();

            holder.favicon = (ImageView) convertView.findViewById(R.id.feed_entry_favicon);
            holder.published = (TextView) convertView.findViewById(R.id.feed_entry_published);
            holder.title = (TextView) convertView.findViewById(R.id.feed_entry_title);
            holder.summary = (TextView) convertView.findViewById(R.id.feed_entry_summary);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.published.setText("" + published);
        holder.title.setText(title);
        holder.summary.setText(content);

        return convertView;
    }

    public String getTitle() {
        return title;
    }

    public long getId() {
        return id;
    }

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class ViewHolder {
        ImageView favicon;
        TextView published;
        TextView title;
        TextView summary;
    }
}
