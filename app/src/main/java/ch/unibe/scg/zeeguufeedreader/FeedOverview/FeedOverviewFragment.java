package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.Feedly.FeedlyAccount;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 *  Home fragment, displays all feeds and categories.
 */
public class FeedOverviewFragment extends Fragment {

    private ArrayList<Category> categories = new ArrayList<>();

    private ExpandableListView expandableListView;
    private FeedOverviewListAdapter adapter;

    private MenuItem refreshItem;

    private FeedOverviewCallbacks callback;

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
        View mainView = (View) inflater.inflate(R.layout.fragment_feed_overview, container, false);
        expandableListView = (ExpandableListView) mainView.findViewById(R.id.feed_overview_listview);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
                // TODO: Implement for normal categories
                Category category = adapter.getGroup(groupPosition);

                if (category instanceof DefaultCategory) {
                    callback.displayFeedEntryList(category, null);
                    return true;
                }

                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                Feed feed = adapter.getChild(groupPosition, childPosition);
                callback.displayFeedEntryList(null, feed);
                return true;
            }
        });

        // Specific menu for this fragment
        setHasOptionsMenu(true);

        return mainView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.feed_overview, menu);

        this.refreshItem = menu.findItem(R.id.action_refresh);

        if (callback.getFeedlyConnectionManager().isSynchronizing())
            callback.animateRefreshButton(menu.findItem(R.id.action_refresh));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_refresh:
                callback.getFeedlyConnectionManager().synchronize();
                callback.animateRefreshButton(item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new FeedOverviewListAdapter(getActivity(), categories);
        expandableListView.setAdapter(adapter);

        restoreExpandedState();
    }

    public void updateSubscriptions(ArrayList<Category> categories) {
        adapter.setCategories(categories);
        this.categories = categories;
        restoreExpandedState();
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public void updateUnreadCount() {
        new Thread(new Runnable() {
            public void run() {
                for (Category category : categories)
                    category.getUnreadCount();

                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                        adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
        restoreExpandedState();
    }

    private void restoreExpandedState() {
        for (int i = 0; i < adapter.getCategories().size(); i++)
            if(adapter.getCategories().get(i).isExpanded())
                expandableListView.expandGroup(i);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (ch.unibe.scg.zeeguufeedreader.FeedOverview.FeedOverviewCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement FeedOverviewCallbacks");
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

        // Recalculate unread count (and update default categories)
        new Thread(new Runnable() {
            public void run() {
                callback.getFeedlyAccount().updateDefaultCategories();

                updateUnreadCount();
            }
        }).start();
    }

    @Override
    public void onStop() {
        super.onStop();

        callback.stopRefreshAnimation(refreshItem);
    }
}
