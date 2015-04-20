package ch.unibe.scg.zeeguufeedreader;

import android.app.Activity;
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
    private Activity activity;
    private FeedItemFragment feedItemFragment;

    // User Information
    private String email = "feed@reader.test";
    private String password = "B22Ddvsqn4YEO9eEetJs";
    private String sessionID;
    private String languageNative;
    private String languageLearning;

    public ZeeguuConnectionManager(Activity activity, FeedItemFragment feedItemFragment) {
        this.activity = activity;
        this.feedItemFragment = feedItemFragment;
        queue = Volley.newRequestQueue(activity);
    }

    public void getSessionId() {
        String urlSessionID = URL + "session/" + email;

        StringRequest request = new StringRequest(Request.Method.POST,
                urlSessionID, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                sessionID = response;
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("session_id", error.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("password", password);
                return params;
            }
        };

        queue.add(request);
    }

    public void getTranslation(String input, String inputLanguageCode, String outputLanguageCode) {
        String urlTranslation = URL + "translate_from_to/" + Uri.encode(input.trim()) + "/" +
                inputLanguageCode + "/" + outputLanguageCode + "?session=" + sessionID;

        StringRequest request = new StringRequest(Request.Method.GET,
                urlTranslation, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                feedItemFragment.setTranslation(response);
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(request);
    }

    public void contributeWithContext(String input, String inputLanguageCode, String translation, String translationLanguageCode,
                                      final String title, final String url, final String context) {
        String urlContribution = URL + "contribute_with_context/" + inputLanguageCode + "/" + Uri.encode(input.trim()) + "/" +
                translationLanguageCode + "/" + Uri.encode(translation) + "?session=" + sessionID;

        StringRequest request = new StringRequest(Request.Method.POST,
                urlContribution, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(activity, "Word saved to your wordlist", Toast.LENGTH_SHORT).show();
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
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
}
