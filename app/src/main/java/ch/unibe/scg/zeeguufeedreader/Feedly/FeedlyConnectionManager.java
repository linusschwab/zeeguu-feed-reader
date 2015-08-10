package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Class to connect with the Feedly API
 */
public class FeedlyConnectionManager {

    private final String URL = "https://sandbox.feedly.com";
    private final String redirectUri = "http://localhost";

    private final String clientId = "sandbox";
    private final String clientSecret = "YNXZHOH3GPYO6DF7B43K"; // (expires on October 1st 2015)

    private RequestQueue queue;
    private FeedlyAccount account;
    private Activity activity;
    private FeedlyCallbacks callback;

    public FeedlyConnectionManager(Activity activity) {
        this.activity = activity;
        this.account = new FeedlyAccount(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedlyCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedlyCallbacks");
        }

        queue = Volley.newRequestQueue(activity);

        // Load user information
        account.load();

        // Get missing information from server
        if (!account.isUserLoggedIn())
            getAuthenticationCode();
        else if (!account.isUserInSession())
            getAuthenticationToken(account.getAuthenticationCode());
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
        if (!isNetworkAvailable())
            return; // ignore here

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
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("feedly_get__token", FeedlyResponseParser.parseErrorMessage(error));
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
        if (!isNetworkAvailable())
            return; // ignore here

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
                public void onResponse(JSONObject response) {
                    // TODO: Update in account
                    callback.displayMessage(response.toString());
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("feedly_refresh_token", FeedlyResponseParser.parseErrorMessage(error));
                }
            }) {
        };

        queue.add(request);
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
