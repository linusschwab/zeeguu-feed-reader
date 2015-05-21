package ch.unibe.scg.zeeguufeedreader.FeedItem;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.util.JsonReader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.StringReader;

import ch.unibe.scg.zeeguufeedreader.Core.NavigationDrawerFragment;
import ch.unibe.scg.zeeguufeedreader.Core.Utility;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.zeeguulibrary.Core.ZeeguuConnectionManager;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedItemFragment extends Fragment {

    private TextView translationBar;
    private WebView webView;
    private Activity activity;

    private String context, title, url;
    private String selection, translation;

    private SharedPreferences sharedPref;
    private boolean browser;

    private FeedItemCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface FeedItemCallbacks {
        void hideKeyboard();
        ZeeguuConnectionManager getConnectionManager();

        // Browser
        boolean isBrowserEnabled();
        NavigationDrawerFragment getNavigationDrawerFragment();
        ActionBar getSupportActionBar();
    }

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set custom action bar layout
        setHasOptionsMenu(true);
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

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);

        // For usability tests
        browser = callback.isBrowserEnabled();

        prepareWebView();

        if (savedInstanceState == null) {
            String url = sharedPref.getString("pref_browser_homepage", "http://zeeguu.unibe.ch");
            if (browser && !url.equals("Feed Item")) {
                if (url.equals(""))
                    webView.loadUrl("http://zeeguu.unibe.ch");
                else if (!url.contains("http://"))
                    webView.loadUrl("http://" + url);
                else
                    webView.loadUrl(url);
            }
            else
                webView.loadData(loadTestHtml(), "text/html", "utf-8");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private void prepareWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        if (sharedPref.getBoolean("pref_browser_viewport", true)) {
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
        }
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
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
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/extract_contribution.js"), null);
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/common/highlight_words.js"), null);
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/common/extract_context.js"), null);
                view.evaluateJavascript(Utility.assetToString(activity, "javascript/common/text_selection.js"), null);
            }
        });
    }

    private String loadTestHtml() {
        String content = "<h2>Title</h2>" +
                "<p>This is <u>underlined</u> text. And \"this\" is a test phrase, that needs to be long enough so that it does not fit on one line.</p>" +
                "<p>This is a <a href=\"http://google.ch\">link</a>.</p>" +
                "<p>Scrolling<br/>Test.</p>" + "<p>1800-Scrolling Test</p>" + "<p>12:00 Scrolling Test</p>" + "<p>16'00-Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                "Test";
        String title = "Feed Item";
        String css = Utility.assetToString(activity, "css/style.css");

        return "<html><head><title>" + title + "</title><style>" + css + "</style></head><body>" + content + "</body></html>";
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedItemCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedItemCallbacks");
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void extractContextFromPage() {
        webView.evaluateJavascript("getContext();", new ValueCallback<String>() {
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
        callback.getConnectionManager().bookmarkWithContext(selection, sharedPref.getString("pref_zeeguu_language_learning", "EN")
                , translation, sharedPref.getString("pref_zeeguu_language_native", "DE"), title, url, context);
    }

    public void setTranslation(final String translation) {
        // UI changes must be done in the main thread
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                translationBar.setText(Html.fromHtml("<h2>" + translation + "</h2>"));
            }
        });
        this.translation = translation;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void highlight(String word) {
        webView.evaluateJavascript("highlight_words_in_page([\"" + word + "\"]);", null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void unhighlight() {
        webView.evaluateJavascript("unhighlight_words_in_page();", null);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        webView.saveState(savedInstanceState);
        savedInstanceState.putString("translation", translation);
    }

    // Getters/Setters
    public TextView getTranslationBar() {
        return translationBar;
    }

    public void setTranslationBar(TextView translationBar) {
        this.translationBar = translationBar;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSelection() {
        return selection;
    }

    public void setSelection(String selection) {
        this.selection = selection;
    }

    public String getTranslation() {
        return translation;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public boolean isBrowser() {
        return browser;
    }

    public void setBrowser(boolean browser) {
        this.browser = browser;
    }

    public FeedItemCallbacks getCallback() {
        return callback;
    }

    public void setCallback(FeedItemCallbacks callback) {
        this.callback = callback;
    }

    // Add action view for usability tests
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!callback.getNavigationDrawerFragment().isDrawerOpen() && browser) {
            menu.clear();
            inflater.inflate(R.menu.browser, menu);

            ActionBar actionBar = callback.getSupportActionBar();
            actionBar.setCustomView(R.layout.actionview_edittext);
            actionBar.setDisplayShowCustomEnabled(true);

            final EditText edittext = (EditText) actionBar.getCustomView().findViewById(R.id.url);
            edittext.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Perform action on key press
                        String url = edittext.getText().toString();
                        String google = "http://www.google.ch/#safe=off&q=";

                        if (!url.contains("."))
                            webView.loadUrl(google + Uri.encode(url));
                        else if (!url.contains("http://"))
                            webView.loadUrl("http://" + url);
                        else
                            webView.loadUrl(url);

                        callback.hideKeyboard();
                        return true;
                    }
                    return false;
                }
            });
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (browser) {
            if (id == R.id.action_refresh) {
                webView.reload();
                return true;
            }
            if (id == R.id.action_back) {
                if (webView.canGoBack())
                    webView.goBack();
                return true;
            }
            if (id == R.id.action_forward) {
                if (webView.canGoForward())
                    webView.goForward();
                return true;
            }
            if (id == R.id.action_unhighlight) {
                unhighlight();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
