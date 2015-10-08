package ch.unibe.scg.zeeguufeedreader.FeedEntryList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntryFragment;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedEntryListFragment extends Fragment implements
        FeedEntryListActionMode.FeedEntryListActionModeCallbacks {

    private ListView listView;
    private FeedEntryListAdapter adapter;

    private Feed feed;
    private ArrayList<FeedEntry> entries = new ArrayList<>();
    private boolean newEntries;

    private FeedEntryListCallbacks callback;

    private ActionMode mode;
    private int selectedItem = -1;

    /**
     * The system calls this when creating the fragment. Within your implementation, you should
     * initialize essential components of the fragment that you want to retain when the fragment
     * is paused or stopped, then resumed.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The system calls this when it's time for the fragment to draw its user interface for the
     * first time. To draw a UI for your fragment, you must return a View from this method that
     * is the root of your fragment's layout. You can return null if the fragment does not
     * provide a UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mainView = (View) inflater.inflate(R.layout.fragment_feed_entry_list, container, false);
        listView = (ListView) mainView.findViewById(R.id.feed_entry_listview);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (newEntries) {
                    callback.updateFeedEntries(entries, feed);
                    newEntries = false;
                    updateEntry(adapter.getItem(position), position);
                }
                else if (!callback.getPagerEntry(position).equals(adapter.getItem(position))) {
                    callback.updateFeedEntries(entries, feed);
                }
                if (position != selectedItem)
                    callback.displayFeedEntry(feed, position);
                if (mode != null) {
                    mode.finish();
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Start ActionMode
                AppCompatActivity activity = (AppCompatActivity) getActivity();

                mode = activity.startSupportActionMode(
                        new FeedEntryListActionMode(view, position, FeedEntryListFragment.this));

                selectedItem = position;
                return true;
            }
        });

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateFeedEntries();

        adapter = new FeedEntryListAdapter(getActivity(), entries);
        listView.setAdapter(adapter);
    }

    private void updateFeedEntries() {
        if (feed != null && feed.getEntries() != null) {
            if (callback.getFeedlyAccount().showUnreadOnly())
                entries = feed.getUnreadEntries();
            else
                entries = feed.getEntries();
        }
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
        entries = new ArrayList<>();
        newEntries = true;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setEntries(ArrayList<FeedEntry> entries) {
        this.entries = entries;
        feed = null;
        newEntries = true;
    }

    public FeedEntry getEntry(int position) {
        return adapter.getItem(position);
    }

    public boolean hasEntries() {
        if (feed != null)
            return feed.getUnreadEntries().size() != 0;
        else
            return entries != null && entries.size() != 0;
    }

    public int getCount() {
        return adapter.getCount();
    }

    public void updateEntry(FeedEntry entryPager, int position) {
        if (position < adapter.getCount()) {
            FeedEntry entryList = getEntry(position);

            if (entryPager.equals(entryList)) {
                markEntryAsRead(entryList);
                updateView(position);
            }
            else {
                if (feed.equals(entryPager.getFeed())) {
                    int positionList = getEntryPosition(entryPager);
                    if (positionList != -1) {
                        markEntryAsRead(getEntry(positionList));
                        updateView(positionList);
                    }
                }
                else
                    markEntryAsRead(entryPager);
            }
        }
        else if (!feed.equals(entryPager.getFeed()))
            markEntryAsRead(entryPager);
    }

    /**
     * @return position of entry or -1 if not found
     */
    public int getEntryPosition(FeedEntry entry) {
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(entry))
                return i;
        }
        return -1;
    }

    public void markEntryAsRead(FeedEntry entry) {
        if (!entry.isRead()) {
            entry.setRead(true);
            callback.getFeedlyAccount().saveFeedEntry(entry);
        }
    }

    public void updateView(int position) {
        adapter.notifyDataSetChanged();
        View view = listView.getChildAt(position);
        adapter.getView(position, view, listView);
    }

    public void updateVisibleViews() {
        adapter.notifyDataSetChanged();

        int first = listView.getFirstVisiblePosition();
        int last = listView.getLastVisiblePosition();

        for (int i = first; i <= last; i++) {
            View view = listView.getChildAt(i);
            adapter.getView(i, view, listView);
        }
    }

    public void onUnreadSwitch() {
        // Feed
        updateFeedEntries();
        // Category
        // TODO: Category

        adapter.setEntries(entries);
        adapter.notifyDataSetChanged();
    }

    public FeedlyAccount getFeedlyAccount() {
        return callback.getFeedlyAccount();
    }

    public void actionModeFinished() {
        mode = null;
        selectedItem = -1;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ch.unibe.scg.zeeguufeedreader.FeedEntryList.FeedEntryListCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedEntryListCallbacks");
        }
    }

    /**
     * The system calls this method as the first indication that the user is leaving the fragment
     * (though it does not always mean the fragment is being destroyed). This is usually where you
     * should commit any changes that should be persisted beyond the current user session
     * (because the user might not come back).
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (feed != null)
            callback.setActionBar(true, feed.getColor());
        else
            callback.setActionBar(true, 0);
    }

    @Override
    public void onStop() {
        super.onStop();

        callback.resetActionBar();
    }
}
