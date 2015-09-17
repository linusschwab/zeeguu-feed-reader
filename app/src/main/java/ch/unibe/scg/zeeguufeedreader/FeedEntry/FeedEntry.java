package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.graphics.Bitmap;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.R;

@DatabaseTable(tableName = "feed_entries")
public class FeedEntry {

    // Id is generated by the database and set on the object
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, columnName = "feed_id", columnDefinition = "integer references feeds(id) on delete cascade")
    private Feed feed;

    @DatabaseField(columnName = "feedly_id")
    private String feedlyId;

    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "content")
    private String content;

    @DatabaseField(columnName = "summary")
    private String summary;

    @DatabaseField(columnName = "url")
    private String url;

    @DatabaseField(columnName = "author")
    private String author;

    @DatabaseField(columnName = "unread")
    private boolean unread = true;

    @DatabaseField(columnName = "date")
    private Date date;

    public FeedEntry() {
        // Empty constructor needed by ormlite
    }

    // TODO: Change constructor?
    public FeedEntry(String title, String content, String url, String author, long timestamp) {
        this.title = title;
        this.content = content;
        this.url = url;
        this.author = author;
        this.date = new Date(timestamp);
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

        Bitmap favicon = feed.getFavicon();
        if (favicon != null) {
            holder.favicon.setVisibility(View.VISIBLE);
            holder.favicon.setImageBitmap(favicon);
        }
        else
            holder.favicon.setVisibility(View.INVISIBLE);

        holder.published.setText(getDateTime());
        holder.title.setText(title);

        if (summary != null && !summary.equals(""))
            holder.summary.setText(Html.fromHtml(summary));
        else
            holder.summary.setText(Html.fromHtml(content));

        return convertView;
    }

    public String getTitle() {
        return title;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public String getDateFull() {
        DateFormat dateFull = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        return dateFull.format(date);
    }

    public String getDateTime() {
        DateFormat time = new SimpleDateFormat("HH:mm");
        return time.format(date);
    }

    public void setDate(long timestamp) {
        this.date = new Date(timestamp);
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
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
        if (unread != entry.unread) return false;
        if (title != null ? !title.equals(entry.title) : entry.title != null) return false;
        if (content != null ? !content.equals(entry.content) : entry.content != null) return false;
        if (summary != null ? !summary.equals(entry.content) : entry.summary != null) return false;
        if (url != null ? !url.equals(entry.url) : entry.url != null) return false;
        if (author != null ? !author.equals(entry.author) : entry.author != null) return false;
        return !(date != null ? !date.equals(entry.date) : entry.date != null);
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (int) (id ^ (id >>> 32));
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (summary != null ? summary.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
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
