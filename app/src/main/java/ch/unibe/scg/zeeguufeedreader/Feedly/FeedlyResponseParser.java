package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;

public class FeedlyResponseParser {

    /**
     *  @param json:
     *  {
     *      "state": "...",
     *      "plan": "standard",
     *      "access_token": "AQAAF4iTvPam_M4_dWheV_5NUL8E...",
     *      "id": "c805fcbf-3acf-4302-a97e-d82f9d7c897f",
     *      "expires_in": 3920,
     *      "refresh_token": "AQAA7rJ7InAiOjEsImEiOiJmZWVk...",
     *      "token_type": "Bearer"
     *  }
     */
    public static Map<String, String> parseAuthenticationToken(JSONObject json) {
        try {
            String refreshToken = "";
            if (json.has("refresh_token"))
                refreshToken = json.getString("refresh_token");

            String accessToken = json.getString("access_token");

            // Calculate expiration time
            long timestamp = System.currentTimeMillis() / 1000;
            long expiresIn = Long.parseLong(json.getString("expires_in"));
            String expirationTime = Long.toString(timestamp + expiresIn);

            String userId = json.getString("id");

            Map<String, String> response = new HashMap<>();
            response.put("refresh_token", refreshToken);
            response.put("access_token", accessToken);
            response.put("access_token_expiration", expirationTime);
            response.put("user_id", userId);

            return response;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param jsonArray: https://developer.feedly.com/v3/categories/#get-the-list-of-all-categories
     */
    public static ArrayList<Category> parseCategories(JSONArray jsonArray) {
        ArrayList<Category> categories = new ArrayList<>();
        try {
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                Category category = new Category(json.getString("label"));
                category.setFeedlyId(json.getString("id"));
                categories.add(category);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return categories;
    }

    /**
     * @param jsonArray: https://developer.feedly.com/v3/subscriptions/#get-the-users-subscriptions
     */
    public static ArrayList<Feed> parseSubscriptions(JSONArray jsonArray, FeedlyAccount account) {
        ArrayList<Feed> feeds = new ArrayList<>();
        try {
            // Loop through all feeds
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                Feed feed = new Feed(json.getString("title"));
                feed.setFeedlyId(json.getString("id"));
                feed.setUrl(json.getString("website"));
                if (json.has("visualUrl"))
                    feed.setImageUrl(json.getString("visualUrl"));

                // Loop through all categories of the current feed
                JSONArray jsonCategories = json.getJSONArray("categories");
                for (int j=0; j < jsonCategories.length(); j++) {
                    JSONObject jsonCategory = jsonCategories.getJSONObject(j);
                    Category category = account.getCategoryById(jsonCategory.getString("id"));

                    if (category != null) {
                        feed.addCategoryToLink(category);
                    }
                }

                feeds.add(feed);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return feeds;
    }

    /**
     * @param jsonObject: https://developer.feedly.com/v3/streams/#get-the-content-of-a-stream
     */
    public static ArrayList<FeedEntry> parseFeedEntries(JSONObject jsonObject, Feed feed) {
        ArrayList<FeedEntry> entries = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                entries.add(parseSingleFeedEntry(feed, json));
            }
        }
        catch (JSONException e) {
            Log.e("feedly_parse_entries", e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }

    /**
     * @param jsonObject: https://developer.feedly.com/v3/streams/#get-the-content-of-a-stream
     */
    public static ArrayList<FeedEntry> parseAllFeedEntries(JSONObject jsonObject, FeedlyAccount account) {
        ArrayList<FeedEntry> entries = new ArrayList<>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("items");
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject jsonFeed = jsonArray.getJSONObject(i);
                Feed feed = account.getFeedById(jsonFeed.getJSONObject("origin").getString("streamId"));

                if (feed != null) {
                    FeedEntry entry = parseSingleFeedEntry(feed, jsonFeed);

                    if (entry != null)
                        entries.add(entry);
                }
            }
        }
        catch (JSONException e) {
            Log.e("feedly_parse_entries", e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }

    /**
     * @param json: https://developer.feedly.com/v3/entries/#get-the-content-of-an-entry
     */
    private static FeedEntry parseSingleFeedEntry(Feed feed, JSONObject json) {
        try {
            // Content
            String content = "No content";
            if (json.has("content"))
                content = json.getJSONObject("content").getString("content");
            else if (json.has("summary"))
                content = json.getJSONObject("summary").getString("content");

            // Author
            String author = "";
            if (json.has("author"))
                author = json.getString("author");

            FeedEntry entry = new FeedEntry(
                    json.getString("title"),
                    content,
                    json.getJSONArray("alternate").getJSONObject(0).getString("href"),
                    author,
                    json.getBoolean("unread"),
                    json.getLong("published"));
            entry.setFeedlyId(json.getString("id"));

            if (json.has("summary"))
                entry.setSummary(json.getJSONObject("summary").getString("content"));

            entry.setFeed(feed);

            return entry;
        }
        catch (JSONException e) {
            Log.e("feedly_parse_entry", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param json: https://developer.feedly.com/v3/markers/#get-the-latest-read-operations-to-sync-local-cache
     */
    public static ArrayList<Feed> parseReadFeeds(JSONObject json, FeedlyAccount account) {
        ArrayList<Feed> feeds = new ArrayList<>();

        try {
            if (json.has("feeds")) {
                JSONArray jsonArray = json.getJSONArray("feeds");
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Feed feed = account.getFeedById(jsonObject.getString("id"));
                    feed.setReadEntriesDate(jsonObject.getLong("asOf"));

                    feeds.add(feed);
                }
            }
        }
        catch (JSONException e) {
            Log.e("feedly_parse_read_feeds", e.getMessage());
            e.printStackTrace();
        }

        return feeds;
    }

    /**
     * @param json: https://developer.feedly.com/v3/markers/#get-the-latest-read-operations-to-sync-local-cache
     */
    public static ArrayList<FeedEntry> parseReadFeedEntries(JSONObject json, FeedlyAccount account) {
        ArrayList<FeedEntry> entries = new ArrayList<>();

        try {
            if (json.has("entries")) {
                JSONArray jsonArray = json.getJSONArray("entries");
                for (int i=0; i < jsonArray.length(); i++) {
                    FeedEntry entry = account.getEntryById(jsonArray.getString(i));
                    if (entry != null && json.getLong("updated") > entry.getReadUpdate()) {
                        entry.syncRead(true);
                        entries.add(entry);
                    }
                }
            }

            if (json.has("unread")) {
                JSONArray jsonArray = json.getJSONArray("unread");
                for (int i=0; i < jsonArray.length(); i++) {
                    FeedEntry entry = account.getEntryById(jsonArray.getString(i));
                    if (entry != null && json.getLong("updated") > entry.getReadUpdate()) {
                        entry.syncRead(false);
                        entries.add(entry);
                    }
                }
            }
        }
        catch (JSONException e) {
            Log.e("feedly_parse_read_entry", e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }

    /**
     * @param json: https://developer.feedly.com/v3/markers/#get-the-latest-tagged-entry-ids
     */
    public static ArrayList<FeedEntry> parseTags(JSONObject json, FeedlyAccount account) {
        ArrayList<FeedEntry> entries = new ArrayList<>();

        try {
            json = json.getJSONObject("taggedEntries");
            Iterator<String> keys = json.keys();

            // Search for favorited entries
            while (keys.hasNext()) {
                String key = keys.next();

                if (key.contains("global.saved")) {
                    JSONArray jsonArray = json.getJSONArray(key);

                    for (int i=0; i < jsonArray.length(); i++) {
                        FeedEntry entry = account.getEntryById(jsonArray.getString(i));

                        if (entry != null) {
                            entry.syncFavorite(true);
                            entries.add(entry);
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            Log.e("feedly_parse_tags", e.getMessage());
            e.printStackTrace();
        }

        return entries;
    }

    public static String parseErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject json = new JSONObject(responseBody);

                return json.getString("errorMessage");

            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
            }
        }
        else if (error.getMessage() != null)
            return error.getMessage();

        return "";
    }
}
