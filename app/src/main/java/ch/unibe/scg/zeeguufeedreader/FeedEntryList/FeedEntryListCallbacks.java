package ch.unibe.scg.zeeguufeedreader.FeedEntryList;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryPagerAdapter;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;

/**
 *  Callback interface that must be implemented by the container activity
 */
public interface FeedEntryListCallbacks {
    void setActionBar(boolean displayBackButton, int actionBarColor);

    void resetActionBar();

    FeedlyAccount getFeedlyAccount();

    FeedEntry getPagerEntry(int position);

    void displayFeedEntry(Feed feed, int position);

    void updateFeedEntries(ArrayList<FeedEntry> entries, Feed feed);
}
