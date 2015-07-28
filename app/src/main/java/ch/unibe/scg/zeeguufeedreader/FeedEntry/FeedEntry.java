package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.text.Html;
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
        holder.summary.setText(Html.fromHtml(content));

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedEntry entry = (FeedEntry) o;

        if (id != entry.id) return false;
        if (date != entry.date) return false;
        if (unread != entry.unread) return false;
        if (title != null ? !title.equals(entry.title) : entry.title != null) return false;
        if (content != null ? !content.equals(entry.content) : entry.content != null) return false;
        if (url != null ? !url.equals(entry.url) : entry.url != null) return false;
        return !(author != null ? !author.equals(entry.author) : entry.author != null);
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + date;
        result = 31 * result + (unread ? 1 : 0);
        return result;
    }

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class FeedEntryViewHolder {
        ImageView favicon;
        TextView published;
        TextView title;
        TextView summary;
    }
}
