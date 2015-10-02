package ch.unibe.scg.zeeguufeedreader.FeedEntry;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;


import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;

/**
 * A pager adapter that is responsible for displaying the feed entry fragments
 */
public class FeedEntryPagerAdapter extends FragmentStatePagerAdapter {

    private Feed feed;
    private ArrayList<FeedEntry> entries = new ArrayList<>();

    private FeedEntryCallbacks callback;

    public FeedEntryPagerAdapter(FragmentManager fragmentManager, Activity activity) {
        super(fragmentManager);

        // Make sure that the interface is implemented in the activity
        try {
            callback = (FeedEntryCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedEntryCallbacks");
        }
    }

    @Override
    public Fragment getItem(int position) {
        FeedEntryFragment fragment = FeedEntryFragment.newInstance(position);
        if (entries.size() != 0)
            fragment.setEntry(entries.get(position));
        return fragment;
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    /**
     * Update ViewPager dynamically
     * See: http://stackoverflow.com/a/17855730
     */
    @Override
    public int getItemPosition(Object object) {
        // TODO: Only if feed changed?
        if (object instanceof FeedEntryFragment) {
            int position = ((FeedEntryFragment) object).getPosition();
            if (position < entries.size()) {
                ((FeedEntryFragment) object).setEntry(entries.get(position));
                ((FeedEntryFragment) object).setTitle();
                ((FeedEntryFragment) object).loadEntry();
            }
        }

        return super.getItemPosition(object);
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;

        if (feed != null) {
            if (callback.getFeedlyAccount().showUnreadOnly())
                entries = feed.getUnreadEntries();
            else
                entries = feed.getEntries();
        }
    }

    public ArrayList<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<FeedEntry> entries) {
        this.entries = entries;
        feed = null;
    }
}