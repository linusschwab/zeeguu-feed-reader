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
    private int date;

    private boolean unread = true;

    public FeedEntry(String title, String content, String url, String author, int date, long id) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.author = author;
        this.date = date;
        this.id = id;
    }

    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        FeedEntryViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed_entry, null);
            holder = new FeedEntryViewHolder();

            holder.favicon = (ImageView) convertView.findViewById(R.id.feed_entry_favicon);
            holder.published = (TextView) convertView.findViewById(R.id.feed_entry_published);
            holder.title = (TextView) convertView.findViewById(R.id.feed_entry_title);
            holder.summary = (TextView) convertView.findViewById(R.id.feed_entry_summary);

            convertView.setTag(holder);
        }
        else {
            holder = (FeedEntryViewHolder) convertView.getTag();
        }

        holder.published.setText("" + date);
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class FeedEntryViewHolder {
        ImageView favicon;
        TextView published;
        TextView title;
        TextView summary;
    }
}
