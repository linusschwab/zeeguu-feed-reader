package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import ch.unibe.scg.zeeguufeedreader.R;

/**
 * Class to connect with the Feedly API
 */
public class FeedlyConnectionManager {

    private final String URL = "http://sandbox.feedly.com";
    private final String clientId = "sandbox";
    private final String clientSecret = "YNXZHOH3GPYO6DF7B43K"; // (expires on October 1st 2015)
    private RequestQueue queue;

    private FeedlyAccount account;
    private Activity activity;
    private FeedlyConnectionManagerCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface FeedlyConnectionManagerCallbacks {
        void feedlyAuthentication(String url);
        void displayErrorMessage(String error, boolean isToast);
        void displayMessage(String message);
    }

    public FeedlyConnectionManager(Activity activity) {
        this.account = new FeedlyAccount(activity);
        this.activity = activity;

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedlyConnectionManagerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedlyConnectionManagerCallbacks");
        }

        queue = Volley.newRequestQueue(activity);

        getAuthenticationCode();
    }

    /**
     * Method that must be called after the activity is restored (for example on screen rotation),
     * otherwise the callbacks will still go to the old/destroyed activity!
     */
    public void onRestore(Activity activity) {
        this.activity = activity;
        callback = (FeedlyConnectionManagerCallbacks) activity;
    }

    /**
     * Gets an authentication code which is needed to use the API
     *
     * GET /v3/auth/auth
     */
    public void getAuthenticationCode() {
        if (!isNetworkAvailable())
            return; // ignore here

        String redirectUri = Uri.encode("http://localhost");
        String scope = Uri.encode("https://cloud.feedly.com/subscriptions");

        String url = String.format(URL + "/v3/auth/auth" + "?response_type=code" +
                "&client_id=%1$s" + "&redirect_uri=%2$s" + "&scope=%3$s", clientId, redirectUri, scope);

        callback.feedlyAuthentication(url);
    }

    public void authenticationSuccessful(String code) {
        callback.displayMessage(activity.getString(R.string.feedly_authentication_successful));
        account.setAuthenticationCode(code);
    }

    // Boolean Checks
    // TODO: Write tests!
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
