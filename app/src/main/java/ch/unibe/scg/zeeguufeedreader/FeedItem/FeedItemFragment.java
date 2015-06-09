package ch.unibe.scg.zeeguufeedreader.FeedItem;

import android.os.Bundle;

import ch.unibe.zeeguulibrary.Core.Utility;
import ch.unibe.zeeguulibrary.WebView.ZeeguuWebViewFragment;

/**
 *  Fragment to display a single article from a feed
 */
public class FeedItemFragment extends ZeeguuWebViewFragment {

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
            webView.loadData(loadTestHtml(), "text/html", "utf-8");
        } else {
            webView.restoreState(savedInstanceState);
        }
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
        String css = Utility.assetToString(getActivity(), "css/style.css");

        return "<html><head><title>" + title + "</title><style>" + css + "</style></head><body>" + content + "</body></html>";
    }
}
