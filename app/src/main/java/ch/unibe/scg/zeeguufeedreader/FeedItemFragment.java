package ch.unibe.scg.zeeguufeedreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.FeedItemCompatibility.TextViewSelection;
import ch.unibe.scg.zeeguufeedreader.FeedItemCompatibility.TranslationActionMode;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedItemFragment extends Fragment {

    private TextView translationBar;
    private WebView webView;

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
        View mainView = inflater.inflate(R.layout.fragment_feed_item, container, false);
        translationBar = (TextView) mainView.findViewById(R.id.feed_item_translation);
        webView = (WebView) mainView.findViewById(R.id.feed_item_content);

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebViewInterface(getActivity(), translationBar), "Android");

        // Force links and redirects to open in the WebView instead of in a browser
        webView.setWebViewClient(new WebViewClient());

        // Load HTML
        String content = "<h2>Title</h2>" +
                "<p>This is <u>underlined</u> text. And this is a test phrase that needs to be long enough so that it does not fit on one line.</p>" +
                "<p>This is a <a href=\"http://google.ch\">link</a>.</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "Test";
        String javascript = "<script>" +
                                "document.addEventListener(\"selectionchange\", function() {" +
                                    "Android.updateTranslation(window.getSelection().toString());" +
                                "}, false);" +
                            "</script>";
        String html = "<html><body>" + javascript + content + "</body></html>";

        webView.loadData(html, "text/html", "utf-8");

        return mainView;
    }

    public TextView getTranslationBar() {
        return translationBar;
    }

    /**
     * The system calls this method as the first indication that the user is leaving the fragment
     * (though it does not always mean the fragment is being destroyed). This is usually where you
     * should commit any changes that should be persisted beyond the current user session
     * (because the user might not come back).
     */
    @Override
    public void onPause() {
        super.onPause();
    }
}
