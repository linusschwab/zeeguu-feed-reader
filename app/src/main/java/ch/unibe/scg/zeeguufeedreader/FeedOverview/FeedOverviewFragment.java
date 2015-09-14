package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.FeedEntry.FeedEntry;
import ch.unibe.scg.zeeguufeedreader.R;

/**
 *  Home fragment, displays all feeds and categories.
 */
public class FeedOverviewFragment extends Fragment {

    ArrayList<Category> categories = new ArrayList<>();

    private ExpandableListView expandableListView;
    private FeedOverviewListAdapter adapter;

    private FeedOverviewCallbacks callback;

    /**
     *  Callback interface that must be implemented by the container activity
     */
    public interface FeedOverviewCallbacks {
        void displayFeedEntryList(Feed feed);
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
        View mainView = (View) inflater.inflate(R.layout.fragment_feed_overview, container, false);
        expandableListView = (ExpandableListView) mainView.findViewById(R.id.feed_overview_listview);

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                Feed feed = (Feed) adapter.getChild(groupPosition, childPosition);
                callback.displayFeedEntryList(feed);
                return true;
            }
        });

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new FeedOverviewListAdapter(getActivity(), categories);
        expandableListView.setAdapter(adapter);
    }

    public void updateSubscriptions(ArrayList<Category> categories) {
        adapter.setCategories(categories);
        this.categories = categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Make sure that the interface is implemented in the container activity
        try {
            callback = (FeedOverviewCallbacks) activity;
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
    }
}
