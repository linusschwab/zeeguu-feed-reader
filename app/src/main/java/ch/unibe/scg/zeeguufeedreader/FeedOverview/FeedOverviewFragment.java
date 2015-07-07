package ch.unibe.scg.zeeguufeedreader.FeedOverview;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import ch.unibe.scg.zeeguufeedreader.R;

/**
 *  Home fragment, displays all feeds and categories.
 */
public class FeedOverviewFragment extends Fragment {

    private ExpandableListView expandableListView;
    private FeedOverviewListAdapter adapter;

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
        expandableListView = (ExpandableListView) mainView.findViewById(R.id.main_list_view);

        return mainView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<Category> list = createTestList();

        adapter = new FeedOverviewListAdapter(getActivity(), list);
        expandableListView.setAdapter(adapter);
    }

    private ArrayList<Category> createTestList() {
        Category category1 = new Category("Test", 123);
        Feed feed11 = new Feed("Test Feed", 11);
        Feed feed12 = new Feed("Test Feed 2", 12);
        category1.addFeed(feed11);
        category1.addFeed(feed12);

        Category category2 = new Category("Hallo", 124);
        Feed feed21 = new Feed("Test Feed", 21);
        Feed feed22 = new Feed("Test Feed 2", 22);
        Feed feed23 = new Feed("Test Feed 3", 22);
        category2.addFeed(feed21);
        category2.addFeed(feed22);
        category2.addFeed(feed23);

        Category category3 = new Category("Kategorie", 125);
        Feed feed31 = new Feed("Test Feed", 31);
        Feed feed32 = new Feed("Test Feed 2", 32);
        Feed feed33 = new Feed("Test Feed 3", 32);
        category3.addFeed(feed31);
        category3.addFeed(feed32);
        category3.addFeed(feed33);

        ArrayList<Category> list = new ArrayList<>();
        list.add(category1);
        list.add(category2);
        list.add(category3);

        return list;
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
}
