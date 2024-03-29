package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.unibe.scg.zeeguufeedreader.Core.ContextManager;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.R;

@DatabaseTable(tableName = "feed_entries")
public class FeedEntry implements Comparable<FeedEntry> {

    // Id is generated by the database and set on the object
    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "feed_id", columnDefinition = "integer references feeds(id) on delete cascade")
    private Feed feed;

    @DatabaseField(columnName = "feedly_id")
    private String feedlyId;

    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "content")
    private String content;

    @DatabaseField(columnName = "content_full")
    private String contentFull;

    @DatabaseField(columnName = "summary")
    private String summary;

    @DatabaseField(columnName = "image")
    private String image;

    @DatabaseField(columnName = "url")
    private String url;

    @DatabaseField(columnName = "author")
    private String author;

    @DatabaseField(columnName = "date")
    private Date date;

    @DatabaseField(columnName = "read")
    private boolean read;

    @DatabaseField(columnName = "read_update")
    private long readUpdate;

    @DatabaseField(columnName = "favorite")
    private boolean favorite;

    @DatabaseField(columnName = "favorite_update")
    private long favoriteUpdate;

    @DatabaseField(columnName = "zeeguu_difficulty_average")
    private Float difficultyAverage;

    @DatabaseField(columnName = "zeeguu_difficulty_median")
    private Float difficultyMedian;

    @DatabaseField(columnName = "zeeguu_learnability_percentage")
    private Float learnabilityPercentage;

    @DatabaseField(columnName = "zeeguu_learnability_count")
    private Integer learnabilityCount;

    public FeedEntry() {
        // Empty constructor needed by ormlite
    }

    // TODO: Change constructor?
    public FeedEntry(String title, String content, String url, String author, boolean unread, long timestamp) {
        this.title = title;
        this.content = content;
        this.summary = createSummaryFromContent();
        this.url = url;
        this.author = author;
        this.read = !unread;
        this.date = new Date(timestamp);
    }

    public View getView(LayoutInflater inflater, View convertView, ViewGroup parent) {
        FeedEntryViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.feed_entry, parent, false);
            holder = new FeedEntryViewHolder();

            holder.favicon = (ImageView) convertView.findViewById(R.id.feed_entry_favicon);
            holder.published = (TextView) convertView.findViewById(R.id.feed_entry_published);
            holder.learnability = (TextView) convertView.findViewById(R.id.feed_entry_learnability);
            holder.difficulty = (ImageView) convertView.findViewById(R.id.feed_entry_difficulty);
            holder.title = (TextView) convertView.findViewById(R.id.feed_entry_title);
            holder.summary = (TextView) convertView.findViewById(R.id.feed_entry_summary);
            holder.favorite = (ImageView) convertView.findViewById(R.id.feed_entry_favorite);

            convertView.setTag(holder);
        }
        else {
            holder = (FeedEntryViewHolder) convertView.getTag();
        }

        // Read/Unread
        if (read) {
            holder.published.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.lightsilver));
            holder.title.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.silver));
            holder.summary.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.lightsilver));
            holder.learnability.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.lightsilver));
            holder.difficulty.setAlpha(0.2f);
        }
        else {
            holder.published.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.gray));
            holder.title.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.darkgray));
            holder.summary.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.gray));
            holder.learnability.setTextColor(ContextCompat.getColor(ContextManager.getContext(), R.color.gray));
            holder.difficulty.setAlpha(0.6f);
        }

        // Favicon
        Bitmap favicon = feed.getFavicon();
        if (favicon != null) {
            holder.favicon.setVisibility(View.VISIBLE);
            holder.favicon.setImageBitmap(favicon);
        }
        else
            holder.favicon.setVisibility(View.INVISIBLE);

        // Date
        holder.published.setText(getDateSimple());

        // Article Recommender
        if (getDifficulty() != null && learnabilityCount != null) {
            holder.learnability.setVisibility(View.VISIBLE);
            holder.difficulty.setVisibility(View.VISIBLE);

            holder.learnability.setText(Integer.toString(learnabilityCount));

            if (getDifficulty() <= 0.3f)
                holder.difficulty.setColorFilter(ContextCompat.getColor(ContextManager.getContext(), R.color.green));
            else if (getDifficulty() <= 0.4f)
                holder.difficulty.setColorFilter(ContextCompat.getColor(ContextManager.getContext(), R.color.yellow));
            else
                holder.difficulty.setColorFilter(ContextCompat.getColor(ContextManager.getContext(), R.color.red));
            }
        else {
            holder.learnability.setVisibility(View.INVISIBLE);
            holder.difficulty.setVisibility(View.INVISIBLE);
        }

        // Title
        holder.title.setText(title);

        // Summary
        holder.summary.setText(summary);

        // Favorite
        if (favorite) {
            holder.favorite.setVisibility(View.VISIBLE);
            holder.favorite.setImageResource(R.drawable.ic_star);
        }
        else {
            holder.favorite.setVisibility(View.GONE);
            holder.favorite.setImageDrawable(null);
        }

        return convertView;
    }

    private String createSummaryFromContent() {
        String newline = System.getProperty("line.separator");

        if (content != null && !content.equals("")) {
            String summary = Html.fromHtml(content).toString();
            summary = summary.replaceAll("￼", ""); // Removes objects like images
            summary = summary.replaceAll(newline, " ");
            return summary.trim();
        }

        return "";
    }

    @Override
    public int compareTo(FeedEntry entry) {
        // TODO: Add option for oldest first (this.date.compareTo(entry.date))
        // Newest first
        return entry.date.compareTo(this.date);
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
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

    public String getContentAsText() {
        // Currently the summary is the full entry content without html
        return summary;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentFull() {
        return contentFull;
    }

    public void setContentFull(String contentFull) {
        this.contentFull = contentFull;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Date getDate() {
        return date;
    }

    public String getDateFull() {
        DateFormat dateFull = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        return dateFull.format(date);
    }

    public String getDateSimple() {
        DateFormat dateSimple = new SimpleDateFormat("dd.MM - HH:mm");
        return dateSimple.format(date);
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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        syncRead(read);
        readUpdate = System.currentTimeMillis();
    }

    public void updateRead(boolean read) {
        this.read = read;
    }

    public void syncRead(boolean read) {
        if (this.read != read) {
            if (read)
                feed.decreaseUnreadCount();
            else
                feed.increaseUnreadCount();
        }

        this.read = read;
    }

    public long getReadUpdate() {
        return readUpdate;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
        favoriteUpdate = System.currentTimeMillis();
    }

    public void syncFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getFavoriteUpdate() {
        return favoriteUpdate;
    }

    public Float getDifficulty() {
        return difficultyAverage;
    }

    public void setDifficultyAverage(Float difficultyAverage) {
        this.difficultyAverage = difficultyAverage;
    }

    public void setDifficultyMedian(Float difficultyMedian) {
        this.difficultyMedian = difficultyMedian;
    }

    public Float getLearnabilityPercentage() {
        return learnabilityPercentage;
    }

    public void setLearnabilityPercentage(Float learnabilityPercentage) {
        this.learnabilityPercentage = learnabilityPercentage;
    }

    public Integer getLearnabilityCount() {
        return learnabilityCount;
    }

    public void setLearnabilityCount(Integer learnabilityCount) {
        this.learnabilityCount = learnabilityCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedEntry entry = (FeedEntry) o;

        if (feed != null ? !feed.equals(entry.feed) : entry.feed != null) return false;
        return !(title != null ? !title.equals(entry.title) : entry.title != null);
    }

    @Override
    public int hashCode() {
        int result = feed != null ? feed.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    // View Holder, see: https://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
    static class FeedEntryViewHolder {
        ImageView favicon;
        TextView published;
        TextView learnability;
        ImageView difficulty;
        TextView title;
        TextView summary;
        ImageView favorite;
    }
}
