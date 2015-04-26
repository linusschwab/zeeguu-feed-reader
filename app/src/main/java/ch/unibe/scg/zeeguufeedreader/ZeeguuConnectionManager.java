package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 *  Class to connect with the Zeeguu API
 */
public class ZeeguuConnectionManager {

    private final String URL = "https://www.zeeguu.unibe.ch/";
    private RequestQueue queue;

    private ZeeguuAccount account;

    private Activity activity;
    private FeedItemFragment feedItemFragment;

    public ZeeguuConnectionManager(Activity activity, FeedItemFragment feedItemFragment) {
        this.account = new ZeeguuAccount(activity);
        this.activity = activity;
        this.feedItemFragment = feedItemFragment;

        queue = Volley.newRequestQueue(activity);
    }

    /**
     *  Gets a session ID which is needed to use the API
     *
     *  @Precondition: user needs to be logged in
     */
    public void getSessionId() {
        if (!account.isUserLoggedIn() || !isNetworkAvailable())
            return; // ignore here, just do nothing

        String urlSessionID = URL + "session/" + account.getEmail();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlSessionID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                account.setSessionID(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.toString().equals("com.android.volley.AuthFailureError"));
                    showLoginDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", account.getPassword());
                return params;
            }
        };

        queue.add(request);
    }

    /**
     *  Translates a given word or phrase from a language to another language
     *
     *  @Precondition: user needs to be logged in and have a session id
     */
    public void translate(final String input, String inputLanguageCode, String outputLanguageCode) {
        if (!account.isUserLoggedIn())
            return; // TODO: Show Login prompt(?) (and get session ID)
        else if (!isNetworkAvailable()) {
            feedItemFragment.setTranslation(activity.getString(R.string.no_internet_connection));
            return;
        }
        else if (!account.isUserInSession()) {
            getSessionId();
            return;
        }
        else if (!isInputValid(input))
            return; // ignore

        // /translate/<from_lang_code>/<to_lang_code>
        String urlTranslation = URL + "translate/" + inputLanguageCode + "/" + outputLanguageCode +
                "?session=" + account.getSessionID();


        StringRequest request = new StringRequest(Request.Method.POST,
                urlTranslation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                feedItemFragment.setTranslation(response);
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("translation", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("word", Uri.encode(input.trim()));
                params.put("url", "");
                params.put("context", "");

                return params;
            }
        };

        queue.add(request);
    }

    public void contributeWithContext(String input, String inputLanguageCode, String translation, String translationLanguageCode,
                                      final String title, final String url, final String context) {
        if (!account.isUserLoggedIn())
            return; // TODO: Show Login prompt (and get session ID)
        else if (!isNetworkAvailable() || !isInputValid(input) || !isInputValid(translation))
            return;
        else if (!account.isUserInSession()) {
            getSessionId();
            return;
        }

        String urlContribution = URL + "contribute_with_context/" + inputLanguageCode + "/" + Uri.encode(input.trim()) + "/" +
                translationLanguageCode + "/" + Uri.encode(translation) + "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlContribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(activity, "Word saved to your wordlist", Toast.LENGTH_SHORT).show();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e("contribute_with_context", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("url", url);
                params.put("context", context);

                return params;
            }
        };

        queue.add(request);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnected();
    }

    private boolean isInputValid(String input) {
        return !(input == null || input.trim().equals(""));
    }

    public void showLoginDialog() {

    }
}
