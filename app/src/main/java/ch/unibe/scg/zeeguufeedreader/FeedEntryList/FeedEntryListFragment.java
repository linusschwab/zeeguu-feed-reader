package ch.unibe.scg.zeeguufeedreader.FeedEntryList;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.FeedOverview.Feed;
import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;

public class FeedEntryListFragment extends Fragment {

    private ListView listView;
    private FeedEntryListAdapter adapter;

    // TODO: display error message if null
    private Feed feed;

    private FeedEntryListCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface FeedEntryListCallbacks {
        void setActionBar(boolean displayBackButton, int actionBarColor);
        void resetActionBar();

        void displayFeedEntry(Feed feed, int position);
    }

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
                callback.displayFeedEntry(feed, position);
            }
        });

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<FeedEntry> entries = new ArrayList<>();
        if (feed.getEntries() != null)
            // TODO: Read/Unread switch
            entries = feed.getUnreadEntries();

        adapter = new FeedEntryListAdapter(getActivity(), entries);
        listView.setAdapter(adapter);
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public FeedEntry getEntry(int position) {
        return adapter.getItem(position);
    }

    public void markEntryAsRead(int position, FeedlyAccount account) {
        // Mark as read
        FeedEntry entry = getEntry(position);
        entry.markAsRead();
        account.saveFeedEntry(entry);

        // Refresh view
        updateView(position);
    }

    public void updateView(int position) {
        adapter.notifyDataSetChanged();
        View view = listView.getChildAt(position);
        adapter.getView(position, view, listView);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedEntryListCallbacks) activity;
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

        callback.setActionBar(true, feed.getColor());
    }

    @Override
    public void onStop() {
        super.onStop();

        callback.resetActionBar();
    }
}
