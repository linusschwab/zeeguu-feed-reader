package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;

/**
 *  Callback interface that must be implemented by the container activity
 */
public interface FeedOverviewCallbacks {
    void displayFeedEntryList(Category category, Feed feed);

    FeedlyAccount getFeedlyAccount();
}
