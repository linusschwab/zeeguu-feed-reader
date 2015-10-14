package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.unibe.scg.zeeguufeedreader.Core.Timer;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Category;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Class to connect with the Feedly API
 */
public class FeedlyConnectionManager {

    private final String URL = "https://sandbox.feedly.com";
    private final String redirectUri = "https://localhost";

    private final String clientId = "sandbox";
    private final String clientSecret = "L5L3EBB9XMVLA8OETMO3"; // (expires on December 1st 2015)

    private RequestQueue queue;
    private FeedlyAccount account;
    private Activity activity;
    private FeedlyCallbacks callback;

    private boolean synchronizing;

    // Timer to measure synchronization duration
    Timer timer = new Timer();

    public FeedlyConnectionManager(Activity activity) {
        this.activity = activity;
        this.account = new FeedlyAccount(activity);
        synchronizing = false;

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedlyCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedlyCallbacks");
        }

        queue = Volley.newRequestQueue(activity);

        loadUserData();

        if (callback.loadBoolean(R.string.pref_feedly_synchronize, true))
            synchronize();
    }

    /**
     * Method that must be called after the activity is restored (for example on screen rotation),
     * otherwise the callbacks will still go to the old/destroyed activity!
     */
    public void onRestore(Activity activity) {
        this.activity = activity;
        callback = (FeedlyCallbacks) activity;
        account.onRestore(activity);
    }

    private void loadUserData() {
        // Load user information
        account.load();
        account.loadCategories();
        account.loadFeeds();
        callback.setSubscriptions(account.getCategories(), false);

        if (account.isProfileSet())
            callback.setAccountHeader(account.getName(), account.getEmail(), account.getPicture());
    }

    public void synchronize() {
        if (!synchronizing) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                // Get missing information from server
                if (!account.isUserLoggedIn())
                    getAuthenticationCode();
                else if (!account.isUserInSession())
                    getAuthenticationToken(account.getAuthenticationCode());
                else if (account.isAccessTokenExpired())
                    refreshAccessToken();
                else if (!account.isProfileSet())
                    getUserProfile();
                else
                    getCategories();
                }
            });

            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();

            synchronizing = true;
            timer.start();
        }
    }

    private void onSynchronizationFinished() {
        synchronizing = false;
        timer.stop();

        // Update UI
        activity.runOnUiThread(new Runnable() {
            public void run() {
                callback.setSubscriptions(account.getCategories(), true);
                callback.displayMessage(activity.getString(R.string.feedly_subscriptions_updated) + " (" + timer.getTimeElapsed() + ")");
            }
        });
    }

    private void onSynchronizationAborted() {
        synchronizing = false;
        timer.stop();
    }

    /**
     * Gets an authentication code which is needed to use the API
     *
     * GET /v3/auth/auth
     */
    public void getAuthenticationCode() {
        if (!isNetworkAvailable())
            return; // ignore here

        String scope = Uri.encode("https://cloud.feedly.com/subscriptions");

        String url = String.format(URL + "/v3/auth/auth" + "?response_type=code" +
                "&client_id=%1$s" + "&redirect_uri=%2$s" + "&scope=%3$s", clientId, redirectUri, scope);

        callback.displayFeedlyAuthentication(url);
    }

    public void authenticationSuccessful(String code) {
        callback.displayMessage(activity.getString(R.string.feedly_authentication_successful));
        account.setAuthenticationCode(code);
        getAuthenticationToken(code);
    }

    public void authenticationFailed(String error) {
        callback.displayMessage(activity.getString(R.string.feedly_authentication_failed) + ": " + error);
    }

    /**
     * Exchanging the authentication code for a refresh token and an access token
     *
     * POST /v3/auth/token
     */
    public void getAuthenticationToken(String authenticationCode) {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }
        if (!isInputValid(authenticationCode)) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/auth/token";

        JSONObject params = new JSONObject();
        try {
            params.put("code", authenticationCode);
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("redirect_uri", redirectUri);
            params.put("state", "");
            params.put("grant_type", "authorization_code");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {
                Map<String, String> response = FeedlyResponseParser.parseAuthenticationToken(json);
                account.setRefreshToken(response.get("refresh_token"));
                account.setAccessToken(response.get("access_token"), response.get("access_token_expiration"));
                account.setUserId(response.get("user_id"));
                account.saveLoginInformation();

                synchronizing = false;
                synchronize();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get__token", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {
        };

        queue.add(request);
    }

    /**
     * Using a refresh token to get a new access token
     *
     * POST /v3/auth/token
     */
    public void refreshAccessToken() {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/auth/token";

        JSONObject params = new JSONObject();
        try {
            params.put("refresh_token", account.getRefreshToken());
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("grant_type", "refresh_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {
                Map<String, String> response = FeedlyResponseParser.parseAuthenticationToken(json);
                account.setAccessToken(response.get("access_token"), response.get("access_token_expiration"));
                account.saveLoginInformation();

                synchronizing = false;
                synchronize();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_refresh_token", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }
        }) {
        };

        queue.add(request);
    }

    /**
     * Revoke the refresh token (logout)
     *
     * POST /v3/auth/token
     *
     * Input:
     * refresh_token: string The refresh token returned in the previous code.
     * client_id: string The clientId obtained during application registration.
     * client_secret: string The client secret obtained during application registration.
     * grant_type: string This field must contain a value of revoke_token.
     */
    public void revokeRefreshToken() {
        if (!isNetworkAvailable())
            return; // ignore here

        String url = URL + "/v3/auth/token";

        JSONObject params = new JSONObject();
        try {
            params.put("refresh_token", account.getRefreshToken());
            params.put("client_id", clientId);
            params.put("client_secret", clientSecret);
            params.put("grant_type", "revoke_token");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                account.logout();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_revoke_token", FeedlyResponseParser.parseErrorMessage(error));
            }
        }) {
        };

        queue.add(request);
    }

    /**
     * Get the Feedly profile of the user
     *
     * GET /v3/profile
     */
    public void getUserProfile() {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/profile";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(final JSONObject json) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        Map<String, String> profile = FeedlyResponseParser.parseProfile(json);

                        if (profile != null) {
                            account.setUserId(profile.get("id"));
                            account.setEmail(profile.get("email"));
                            account.setName(profile.get("name"));

                            if (!profile.get("picture").equals(""))
                                getPicture(profile.get("picture"));
                            else {
                                activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        callback.setAccountHeader(account.getName(), account.getEmail(), account.getPicture());
                                    }
                                });
                            }
                        }

                        account.saveLoginInformation();

                        getCategories();
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_profile", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }

        };

        queue.add(request);
    }

    /**
     * Get a list of all user categories
     *
     * GET /v3/categories
     */
    public void getCategories() {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/categories";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(final JSONArray response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        // Parse and synchronize categories
                        ArrayList<Category> categories = FeedlyResponseParser.parseCategories(response);
                        account.synchronizeCategories(categories);

                        getSubscriptions();
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_categories", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }
        };

        queue.add(request);
    }

    /**
     * Get a list of all feeds the user subscribed to
     *
     * GET /v3/subscriptions
     */
    public void getSubscriptions() {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/subscriptions";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(final JSONArray response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        // Parse and synchronize feeds
                        ArrayList<Feed> feeds = FeedlyResponseParser.parseSubscriptions(response, account);
                        account.synchronizeFeeds(feeds);

                        getAllFavicons();
                        getAllFeedEntries(500);
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_feeds", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }
        };

        queue.add(request);
    }

    /**
     * Get the defined amount of entries for a feed.
     * No authentication needed.
     *
     * GET /v3/streams/contents?streamId=streamId
     */
    public void getFeedEntries(final Feed feed, int count) {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }
        if (count > 1000)
            count = 1000;

        String url = URL + "/v3/streams/contents?streamId=" + Uri.encode(feed.getFeedlyId()) +
                           "&count=" + count;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO: save
                FeedlyResponseParser.parseFeedEntries(response, feed);
                callback.setSubscriptions(account.getCategories(), true);
                callback.displayMessage(activity.getString(R.string.feedly_subscriptions_updated));
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_all_entries", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        });

        queue.add(request);
    }

    /**
     * Get the defined amount of entries (sorted by date, newest first)
     *
     * GET /v3/streams/contents?streamId=streamId
     */
    public void getAllFeedEntries(int count) {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }
        if (count > 1000) // TODO: Multiple API calls if more than 1000
            count = 1000;

        String url = URL + "/v3/streams/contents?streamId=" +
                           Uri.encode("user/" + account.getUserId() + "/category/global.all") +
                           "&count=" + count;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(final JSONObject response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        // Parse and synchronize entries
                        ArrayList<FeedEntry> entries = FeedlyResponseParser.parseAllFeedEntries(response, account);
                        account.synchronizeFeedEntries(entries);

                        // Sync read/favorited entries
                        getLatestReadOperations(account.getLastReadSync());
                        getLatestTags(account.getLastFavoriteSync());

                        account.loadCategoryFeed();

                        onSynchronizationFinished();
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_feed_entries", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }
        };

        queue.add(request);
    }

    /**
     * Get the tags (marked entries etc.) newer than the defined timestamp
     *
     * GET /v3/markers/reads
     */
    public void getLatestReadOperations(long newerThan) {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/markers/reads" + "?newerThan=" + newerThan;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(final JSONObject response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        ArrayList<Feed> feeds = FeedlyResponseParser.parseReadFeeds(response, account);
                        ArrayList<FeedEntry> entries = FeedlyResponseParser.parseReadFeedEntries(response, account);

                        account.markFeedsAsRead(feeds);
                        account.saveFeedEntries(entries);

                        // Sync local read operations with Feedly
                        markEntriesAsRead(account.getLatestReadEntries(account.getLastReadSync()));
                        markEntriesAsUnread(account.getLatestUnreadEntries(account.getLastReadSync()));

                        account.readSyncFinished();
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_latest_reads", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }

        };

        queue.add(request);
    }

    /**
     * Get the tags (marked entries etc.) newer than the defined timestamp
     *
     * GET /v3/markers/tags
     */
    public void getLatestTags(long newerThan) {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/markers/tags" + "?newerThan=" + newerThan;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(final JSONObject response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        ArrayList<FeedEntry> entries = FeedlyResponseParser.parseTags(response, account);
                        account.saveFeedEntries(entries);

                        // Sync existing favorites with Feedly
                        markEntriesAsFavorited(account.getLatestFavoritedEntries(account.getLastFavoriteSync()));
                        markEntriesAsUnfavorited(account.getLatestUnfavoritedEntries(account.getLastFavoriteSync()));

                        account.favoriteSyncFinished();
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_latest_tags", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }

        };

        queue.add(request);
    }

    public void markEntriesAsRead(final ArrayList<FeedEntry> entries) {
        markEntriesAs("markAsRead", entries);
    }

    public void markEntriesAsUnread(final ArrayList<FeedEntry> entries) {
        markEntriesAs("keepUnread", entries);
    }

    public void markEntriesAsFavorited(final ArrayList<FeedEntry> entries) {
        markEntriesAs("markAsSaved", entries);
    }

    public void markEntriesAsUnfavorited(final ArrayList<FeedEntry> entries) {
        markEntriesAs("markAsUnsaved", entries);
    }

    /**
     * Mark entries as read/unread or saved/unsaved (favorite) on the Feedly server
     *
     * POST /v3/markers
     */
    private void markEntriesAs(final String action, final ArrayList<FeedEntry> entries) {
        if (!isNetworkAvailable()) {
            onSynchronizationAborted();
            return;
        }

        String url = URL + "/v3/markers";

        JSONObject params = new JSONObject();
        try {
            params.put("action", action);
            params.put("type", "entries");
            params.put("entryIds", getFeedlyIds(entries));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                url, params, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject json) {
                // TODO: Check if ok
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_mark_as", FeedlyResponseParser.parseErrorMessage(error));
                onSynchronizationAborted();
            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return authorizationHeader();
            }

        };

        queue.add(request);
    }

    /**
     * Get the metadata for all feeds
     *
     * POST /v3/feeds/.mget
     */
    public void getFeeds() {
        // TODO: Get feed language and automatically switch Zeeguu language or disable if in native language
    }

    /**
     * Get the metadata of a feed
     *
     * GET /v3/feeds/:feedId
     */
    public void getFeed(Feed feed) {

    }

    /**
     * Generate the header with the authorization code
     */
    private Map<String, String> authorizationHeader() {
        Map<String, String> params = new HashMap<>();
        params.put("Authorization", "OAuth " + account.getAccessToken());
        return params;
    }

    public void getAllFavicons() {
        ArrayList<Feed> feeds = account.getFeeds();

        for (Feed feed : feeds) {
            if (feed.getFavicon() == null)
                getFavicon(feed, true);
        }
    }

    /**
     * Gets the favicon, either from the feed url directly or from Google
     */
    public void getFavicon(final Feed feed, final boolean direct) {
        String url = feed.getUrl();
        String faviconUrl;

        // TODO: Get Apple touch icon if available
        if (!direct)
            faviconUrl = "http://www.google.com/s2/favicons?domain=" + Uri.encode(url);
        else {
            if (url.charAt(url.length()-1) == '/')
                faviconUrl = url + "favicon.ico";
            else
                faviconUrl = url + "/favicon.ico";
        }

        ImageRequest request = new ImageRequest(faviconUrl, new Response.Listener<Bitmap>() {

            @Override
            public void onResponse(final Bitmap response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        feed.setFavicon(response);
                        account.saveFeed(feed);
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, 128, 128, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // Try again using the Google favicon service
                if (direct)
                    getFavicon(feed, false);
                else
                    Log.e("feedly_get_favicon", error.toString());
            }

        });

        queue.add(request);
    }

    /**
     * Gets the Feedly profile picture
     */
    public void getPicture(String url) {

        ImageRequest request = new ImageRequest(url, new Response.Listener<Bitmap>() {

            @Override
            public void onResponse(final Bitmap response) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        account.setPicture(response);
                        account.saveLoginInformation();

                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                            callback.setAccountHeader(account.getName(), account.getEmail(), account.getPicture());
                            }
                        });
                    }
                });

                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

        }, 512, 512, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get_picture", error.toString());
            }

        });

        queue.add(request);
    }

    // Helper methods
    private JSONArray getFeedlyIds(ArrayList<FeedEntry> entries) {
        ArrayList<String> feedlyIds = new ArrayList<>();
        for (FeedEntry entry : entries)
            feedlyIds.add(entry.getFeedlyId());

        return new JSONArray(feedlyIds);
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private boolean isInputValid(String input) {
        return !(input == null || input.trim().equals(""));
    }

    public boolean isSynchronizing() {
        return synchronizing;
    }

    // Getter/Setter
    public FeedlyAccount getAccount() {
        return account;
    }

    public void setAccount(FeedlyAccount account) {
        this.account = account;
    }
}