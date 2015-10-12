package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.view.MenuItem;

import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyConnectionManager;

/**
 *  Callback interface that must be implemented by the container activity
 */
public interface FeedOverviewCallbacks {
    void displayFeedEntryList(Category category, Feed feed);

    void animateRefreshButton(MenuItem item);
    void stopRefreshAnimation(MenuItem item);

    FeedlyConnectionManager getFeedlyConnectionManager();
    FeedlyAccount getFeedlyAccount();
}
