package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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

    private FragmentManager fragmentManager;
    private ZeeguuLoginDialog zeeguuLoginDialog;
    private SharedPreferences sharedPref;

    private String selection, translation;

    public ZeeguuConnectionManager(Activity activity, FeedItemFragment feedItemFragment) {
        this.account = new ZeeguuAccount(activity);
        this.activity = activity;
        this.feedItemFragment = feedItemFragment;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        // Login Dialog
        fragmentManager = activity.getFragmentManager();
        zeeguuLoginDialog = (ZeeguuLoginDialog) fragmentManager.findFragmentByTag("zeeguuLoginDialog");
        if (zeeguuLoginDialog == null) zeeguuLoginDialog = new ZeeguuLoginDialog();

        queue = Volley.newRequestQueue(activity);

        // Load user information
        account.load();

        // Get missing information from server
        if (!account.isUserLoggedIn())
            showLoginDialog(activity.getString(R.string.login_zeeguu_title));
        else if (!account.isUserInSession())
            getSessionId(account.getEmail(), account.getPassword());
        else if (!account.isLanguageSet())
            getUserLanguages();
    }

    /**
     *  Gets a session ID which is needed to use the API
     */
    public void getSessionId(String email, String password) {
        if (!isNetworkAvailable())
            return; // ignore here

        account.setEmail(email);
        account.setPassword(password);

        String urlSessionID = URL + "session/" + email;

        StringRequest request = new StringRequest(Request.Method.POST,
                urlSessionID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                account.setSessionID(response);
                account.saveLoginInformation();
                getUserLanguages();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                account.setEmail("");
                account.setPassword("");
                showLoginDialog("Wrong email or password");
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
        if (!account.isUserLoggedIn()) {
            feedItemFragment.setTranslation(activity.getString(R.string.no_login));
            return;
        }
        else if (!isNetworkAvailable()) {
            feedItemFragment.setTranslation(activity.getString(R.string.no_internet_connection));
            return;
        }
        else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }
        else if (!isInputValid(input))
            return; // ignore
        else if (!isDifferentSelection(input)) {
            feedItemFragment.setTranslation(translation);
            return;
        }

        selection = input;

        // /translate/<from_lang_code>/<to_lang_code>
        String urlTranslation = URL + "translate/" + inputLanguageCode + "/" + outputLanguageCode +
                "?session=" + account.getSessionID();

        StringRequest request = new StringRequest(Request.Method.POST,
                urlTranslation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                feedItemFragment.setTranslation(response);
                translation = response;
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
        if (!account.isUserLoggedIn()) {
            showLoginDialog(activity.getString(R.string.login_zeeguu_title));
            return;
        }
        else if (!isNetworkAvailable() || !isInputValid(input) || !isInputValid(translation))
            return;
        else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        if (sharedPref.getBoolean("pref_zeeguu_highlight_words", true))
            feedItemFragment.highlight(input);

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

    private void getUserLanguages() {
        if (!account.isUserLoggedIn() || !isNetworkAvailable()) {
            return;
        }
        else if (!account.isUserInSession()) {
            getSessionId(account.getEmail(), account.getPassword());
            return;
        }

        String urlLanguage = URL + "learned_and_native_language" + "?session=" + account.getSessionID();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, urlLanguage,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            account.setLanguageNative(response.getString("native"));
                            account.setLanguageLearning(response.getString("learned"));
                            account.saveLanguages();
                        }
                        catch (JSONException error) {
                            Log.e("get_user_language", error.toString());
                        }
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("get_user_language", error.toString());
                    }
                });

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

    private boolean isDifferentSelection(String input) {
        return !(input.equals(selection));
    }

    public void showLoginDialog(String title) {
        zeeguuLoginDialog.setTitle(title);
        zeeguuLoginDialog.show(fragmentManager, "zeeguuLoginDialog");
    }

    public ZeeguuAccount getAccount() {
        return account;
    }

    public void setAccount(ZeeguuAccount account) {
        this.account = account;
    }
}
