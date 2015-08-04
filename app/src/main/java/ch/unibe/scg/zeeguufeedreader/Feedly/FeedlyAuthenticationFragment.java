package ch.unibe.scg.zeeguufeedreader.Feedly;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ch.unibe.scg.zeeguufeedreader.R;

public class FeedlyAuthenticationFragment extends Fragment {

    private WebView webView;
    private String url;

    private FeedlyAuthenticationCallbacks callback;

    /**
     * Callback interface that must be implemented by the container activity
     */
    public interface FeedlyAuthenticationCallbacks {
        void authenticationSuccessful(String code);
    }

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The system calls this when it's time for the fragment to draw its user interface for the
     * first time. To draw a UI for your fragment, you must return a View from this method that
     * is the root of your fragment's layout. You can return null if the fragment does not
     * provide a UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View mainView = (View) inflater.inflate(R.layout.fragment_feedly_authentication, container, false);
        webView = (WebView) mainView.findViewById(R.id.feedly_authentication_webview);

        authenticate(url);

        return mainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedlyAuthenticationCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedlyAuthenticationCallbacks");
        }
    }

    private void authenticate(String url) {
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // Check if the current url is the callback url
                String parameterCode = "?code=";
                String parameterState = "&state=";

                int start = url.indexOf(parameterCode);
                int end = url.indexOf(parameterState);

                if (start > -1) {
                    webView.stopLoading();

                    // Get the code parameter
                    String code;
                    if (end > -1)
                        code = url.substring(start + parameterCode.length(), end);
                    else
                        code = url.substring(start + parameterCode.length(), url.length());

                    callback.authenticationSuccessful(code);
                }
            }
        });
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    public boolean goBack() {
        if (webView == null)
            return true;
        else if (webView.canGoBack()) {
            webView.goBack();
            return false;
        } else
            return true;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
