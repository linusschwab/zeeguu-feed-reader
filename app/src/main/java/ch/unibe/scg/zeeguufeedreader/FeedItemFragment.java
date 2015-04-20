package ch.unibe.scg.zeeguufeedreader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.StringReader;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedItemFragment extends Fragment {

    private TextView translationBar;
    private WebView webView;
    private Activity activity;

    private String context, title, url;
    private String selection, translation;

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
        activity = getActivity();

        // Enable Javascript
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebViewInterface(activity), "Android");

        // Force links and redirects to open in the WebView instead of in a browser, inject css and javascript
        webView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // css
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/injectCSS.js"), null);
                String css = Utility.assetToString(activity, "css/highlight.css").replace("\n", "").replace("\r", "").trim();
                view.evaluateJavascript("injectCSS(\"" + css + "\");", null);
                // javascript
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/jquery-2.1.3.min.js"), null);
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/selectionChangeListener.js"), null);
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/highlightWords.js"), null);
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/extractContext.js"), null);
            }
        });

        // Load HTML
        String content = "<h2>Title</h2>" +
                "<p>This is <u>underlined</u> text. And \"this\" is a test phrase that needs to be long enough so that it does not fit on one line.</p>" +
                "<p>This is a <a href=\"http://google.ch\">link</a>.</p>" +
                "<p>Scrolling Test.</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "Test";
        String title = "Feed Item";
        String css = Utility.assetToString(activity, "css/style.css");
        String html = "<html><head><title>" + title + "</title><style>" + css + "</style></head><body>" + content + "</body></html>";

        webView.loadData(html, "text/html", "utf-8");

        return mainView;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void extractContextFromPage() {
        webView.evaluateJavascript("extractContext();", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                JsonReader reader = new JsonReader(new StringReader(value));
                try {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        String name = reader.nextName();
                        if (name.equals("term"))
                            selection = reader.nextString();
                        else if (name.equals("context"))
                            context = reader.nextString();
                        else if (name.equals("title"))
                            title = reader.nextString();
                        else if (name.equals("url"))
                            url = reader.nextString();
                        else
                            reader.skipValue();
                    }
                    reader.endObject();
                }
                catch (IOException e) {}

                submitContext();
            }
        });
    }

    public void submitContext() {
        MainActivity main = (MainActivity) activity;
        main.getConnectionManager().contributeWithContext(selection, "EN", translation, "DE", title, url, context);
    }

    public void setTranslation(String translation) {
        translationBar.setText(Html.fromHtml("<h2>" + translation + "</h2>"));
        this.translation = translation;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void highlight() {
        webView.evaluateJavascript("highlight_words([window.getSelection().toString()]);", null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void unhighlight() {
        webView.evaluateJavascript("unhighlight_words();", null);
    }

    public TextView getTranslationBar() {
        return translationBar;
    }

    public WebView getWebView() {
        return webView;
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
        }
        else
            return true;
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
