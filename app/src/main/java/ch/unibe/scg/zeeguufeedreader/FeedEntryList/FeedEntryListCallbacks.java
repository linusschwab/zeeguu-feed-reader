package ch.unibe.scg.zeeguufeedreader.FeedEntryList;

import android.view.MenuItem;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyConnectionManager;

/**
 *  Callback interface that must be implemented by the container activity
 */
public interface FeedEntryListCallbacks {
    void setActionBar(boolean displayBackButton, int actionBarColor);
    void resetActionBar();

    void animateRefreshButton(MenuItem item);
    void stopRefreshAnimation(MenuItem item);

    FeedlyConnectionManager getFeedlyConnectionManager();
    FeedlyAccount getFeedlyAccount();

    FeedEntry getPagerEntry(int position);
    void displayFeedEntry(Feed feed, int position);
    void updateFeedEntries(ArrayList<FeedEntry> entries, Feed feed);
}
