package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.Core.DisplayUtility;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.zeeguulibrary.Core.Utility;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedEntryFragment extends ZeeguuWebViewFragment {

    private FeedEntry entry;
    private boolean isFeedEntry;
    private String firstUrl;

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final FrameLayout panelFrame = (FrameLayout) getActivity().findViewById(R.id.panel);
        this.enableTitle(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // Disable transparent header for websites
                if (isLocalUrl(url) && !url.equals("about:blank")) {
                    panelFrame.setPadding(0, 0, 0, 0);
                    isFeedEntry = true;
                }
                else
                    panelFrame.setPadding(0, (int) DisplayUtility.dpToPx(getActivity(), 68), 0, 0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url.equals("about:blank")) {
                    if (firstUrl.equals(""))
                        webView.goBack();
                    else {
                        webView.loadUrl(firstUrl);
                        firstUrl = "";
                    }
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (isFeedEntry) {
                    webView.loadUrl("about:blank");
                    isFeedEntry = false;
                    firstUrl = url;
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else
            loadEntry();
    }

    private String loadHtml() {
        String title = entry.getTitle();
        String url = entry.getUrl();
        String date = entry.getDateFull() + " - " + entry.getDateTime();
        String feed = entry.getFeed().getName();
        String author = entry.getAuthor();
        String content = entry.getContent();

        String css = Utility.assetToString(getActivity(), "css/style.css");

        if (title != null && content != null) {
            String html = "<html><head><style>" + css + "</style></head><body>" +
                          "<a class='entry_title' href='" + url + "'><div class='entry_header'>" +
                          "<span class='entry_info'>" + date + "</span>" +
                          "<h1>" + title + "</h1>" +
                          "<span class='entry_info'>" + feed + " by " + author + "</span>" +
                          "</div></a>" + content + "</body></html>";

            return html;
        }
        else
            return "";
    }

    public FeedEntry getEntry() {
        return entry;
    }

    public void setEntry(FeedEntry entry) {
        if (this.entry == null)
            this.entry = entry;
        else if (!this.entry.equals(entry)) {
            this.entry = entry;
            loadEntry();
        }
    }

    public void loadEntry() {
        if (this.isAdded() && this.entry != null) {
            TextView panel_entry_title = (TextView) getActivity().findViewById(R.id.panel_entry_title);
            panel_entry_title.setText(entry.getTitle());
            TextView panel_feed_title = (TextView) getActivity().findViewById(R.id.panel_feed_title);
            panel_feed_title.setText(entry.getFeed().getName());
            webView.loadDataWithBaseURL("file:///android_asset/", loadHtml(), "text/html", "utf-8", "FeedEntry");
        }
    }

    /**
     * Allow to use the Android back button to navigate back in the WebView
     */
    @Override
    public boolean goBack() {
        if (webView == null)
            return true;
        else if (webView.canGoBack() && !isFeedEntry) {
            webView.goBack();
            return false;
        } else
            return true;
    }

    private boolean isLocalUrl(String url) {
        return url.equals("file:///android_asset/") || url.contains("data:text/html");
    }
}

