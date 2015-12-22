package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.Core.Tools;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;
import ch.unibe.zeeguulibrary.Core.Utility;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewClient;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedEntryFragment extends ZeeguuWebViewFragment {

    private FeedEntry entry;
    private boolean newEntry;

    private boolean isFeedEntry;
    private String firstUrl = "";

    private RelativeLayout panelHeader;
    private FrameLayout panelContent;

    private TextView panelEntryTitle;
    private TextView panelFeedTitle;
    private ImageView panelFavicon;

    private int headerColor;

    // Position in the ViewPager
    private int position;

    /**
     * Create a new instance of FeedEntryFragment, providing "position"
     * as an argument.
     */
    static FeedEntryFragment newInstance(int position) {
        FeedEntryFragment fragment = new FeedEntryFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            position = getArguments().getInt("position");

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
        View mainView = inflater.inflate(R.layout.fragment_feed_entry, container, false);
        translationBar = (RelativeLayout) mainView.findViewById(R.id.webview_translation_bar);
        translationView = (TextView) mainView.findViewById(R.id.webview_translation);
        bookmarkButton = (ImageView) mainView.findViewById(R.id.webview_bookmark);
        webView = (WebView) mainView.findViewById(R.id.webview_content);
        progressBar = (ProgressBar) mainView.findViewById(R.id.webview_progress_bar);
        panelHeader = (RelativeLayout) mainView.findViewById(R.id.panel_header);
        panelContent = (FrameLayout) mainView.findViewById(R.id.panel_content);

        panelEntryTitle = (TextView) mainView.findViewById(R.id.panel_entry_title);
        panelFeedTitle = (TextView) mainView.findViewById(R.id.panel_feed_title);
        panelFavicon = (ImageView) mainView.findViewById(R.id.panel_favicon);

        if (headerColor != 0)
            onPanelExpandend(headerColor);

        setTitle();

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.enableTitle(false);

        webView.setWebViewClient(new ZeeguuWebViewClient(getActivity(), callback, webView, false) {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);

                // Disable transparent header for websites
                if (isLocalUrl(url) && !url.equals("about:blank")) {
                    panelContent.setPadding(0, 0, 0, 0);
                    isFeedEntry = true;
                } else
                    panelContent.setPadding(0, (int) Tools.dpToPx(getActivity(), 68), 0, 0);
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

        if (savedInstanceState != null && !newEntry) {
            webView.restoreState(savedInstanceState);
        }
        else {
            loadEntry();
        }
    }

    @Override
    public void submitContext() {
        if (isFeedEntry) setUrl(entry.getUrl());
        super.submitContext();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        webView.saveState(savedInstanceState);
    }

    private String loadHtml() {
        String newline = System.getProperty("line.separator");

        String title = entry.getTitle();
        String url = entry.getUrl();
        String date = entry.getDateFull() + " - " + entry.getDateTime();
        String feed = entry.getFeed().getName();
        String author = entry.getAuthor();
        String content = entry.getContent();
        String contentFull = entry.getContentFull();
        String image = entry.getImage();

        String css = Utility.assetToString(getActivity(), "css/style.css");

        if (title != null && content != null) {
            // Title block
            String html = "<html><head><style>" + css + "</style><title>" + title + "</title></head><body>" +
                          "<a class='entry_title' href='" + url + "'><div class='entry_header'>" +
                          "<span class='entry_info'>" + date + "</span>" +
                          "<h1>" + title + "</h1><span class='entry_info'>" + feed;
            // Author
            if (!author.equals(""))
                html += " by " + author;
            html += "</span></div></a>";

            // Content
            if (contentFull != null && (contentFull.length() > entry.getContentAsText().length())) {
                if (image != null)
                    html += "<img src=\"" + image + "\">";
                html += "<p>" + contentFull.replace(newline, "<br>") + "</p>";
            }
            else
                html += content;
            html += "</body></html>";

            return html;
        }
        else
            return "";
    }

    public FeedEntry getEntry() {
        return entry;
    }

    public void setEntry(FeedEntry entry) {
        this.entry = entry;
        newEntry = true;
    }

    public void loadEntry() {
        if (this.isAdded() && this.entry != null) {
            webView.loadDataWithBaseURL("file:///android_asset/", loadHtml(), "text/html", "utf-8", "FeedEntry");
            newEntry = false;
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

    public void setTitle() {
        panelEntryTitle.setText(entry.getTitle());
        panelFeedTitle.setText(entry.getFeed().getName());

        Bitmap favicon = entry.getFeed().getFavicon();
        if (favicon != null) {
            panelFavicon.setVisibility(View.VISIBLE);
            panelFavicon.setImageBitmap(favicon);
        }
        else
            panelFavicon.setVisibility(View.INVISIBLE);
    }

    public RelativeLayout getPanelHeader() {
        return panelHeader;
    }

    public void setHeaderColor(int headerColor) {
        this.headerColor = headerColor;
    }

    public void onPanelExpandend(int color) {
        if (panelHeader != null) {
            // Set panel color
            panelHeader.setBackgroundColor(Tools.transparency(color, 0.9));

            // Set text color
            panelEntryTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
            panelFeedTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }

    public void onPanelCollapsed() {
        if (panelHeader != null) {
            // Set panel color
            panelHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));

            // Set text color
            panelEntryTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_primary));
            panelFeedTitle.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_primary));
        }
    }

    public int getPosition() {
        return position;
    }
}

