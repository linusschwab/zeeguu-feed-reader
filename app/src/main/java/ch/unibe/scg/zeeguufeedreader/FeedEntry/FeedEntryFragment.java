package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import ch.unibe.scg.zeeguufeedreader.R;
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
        this.enableTitle(false);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else
            loadEntry();
    }

    private String loadHtml() {
        String title = entry.getTitle();
        String content = entry.getContent();
        String css = Utility.assetToString(getActivity(), "css/style.css");

        if (title != null && content != null)
            return "<html><head><style>" + css + "</style></head><body>" + content + "</body></html>";
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
            TextView panel_title = (TextView) getActivity().findViewById(R.id.panel_title);
            panel_title.setText(entry.getTitle());
            webView.loadDataWithBaseURL(null, loadHtml(), "text/html", "utf-8", null);
        }
    }
}

