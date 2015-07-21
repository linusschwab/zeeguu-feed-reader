package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.os.Bundle;

import ch.unibe.zeeguulibrary.Core.Utility;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedEntryFragment extends ZeeguuWebViewFragment {

    private FeedEntry entry;

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

        if (savedInstanceState == null) {
            webView.loadData(loadHtml(), "text/html", "utf-8");
        } else {
            webView.restoreState(savedInstanceState);
        }
    }

    private String loadHtml() {
        String title = entry.getTitle();
        String content = entry.getContent();
        String css = Utility.assetToString(getActivity(), "css/style.css");

        if (title != null && content != null)
            return "<html><head><title>" + title + "</title><style>" + css + "</style></head><body>" + content + "</body></html>";
        else
            return "";
    }

    public void setEntry(FeedEntry entry) {
        this.entry = entry;
    }
}

