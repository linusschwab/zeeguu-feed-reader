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

    private ArrayList<Category> createTestList() {
        Category category1 = new Category("Test", 123);
        Feed feed11 = new Feed("Test Feed", 11);
        feed11.setColor(getResources().getColor(R.color.darkred));
        feed11.setEntries(createTestFeedEntries());
        Feed feed12 = new Feed("Test Feed 2", 12);
        feed12.setColor(getResources().getColor(R.color.navy));
        feed12.setEntries(createTestFeedEntries());
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

    private ArrayList<FeedEntry> createTestFeedEntries() {
        FeedEntry test1 = new FeedEntry("Test", createTestContent(), "", "Author", 11112015, 101);
        FeedEntry test2 = new FeedEntry("Test Hallo", createTestContent(), "", "Author", 11112015, 102);
        FeedEntry test3 = new FeedEntry("Test Hi", "Test Test Test Test Test Test Test Test", "", "Author", 11112015, 103);
        FeedEntry test4 = new FeedEntry("Test 123", "Test Test Test Test Test Test Test Test", "", "Author", 11112015, 104);
        FeedEntry test5 = new FeedEntry("Test Entry", "Test Test Test Test Test Test Test Test", "", "Author", 11112015, 105);

        ArrayList<FeedEntry> list = new ArrayList<>();

        list.add(test1);
        list.add(test2);
        list.add(test3);
        list.add(test4);
        list.add(test5);

        return list;
    }

    private String createTestContent() {
        String content =    "<p>This is <u>underlined</u> text. And \"this\" is a test phrase, that needs to be long enough so that it does not fit on one line.</p>" +
                            "<p>This is a <a href=\"http://google.ch\">link</a>.</p>" +
                            "<p>Scrolling<br/>Test.</p>" + "<p>1800-Scrolling Test</p>" + "<p>12:00 Scrolling Test</p>" + "<p>16'00-Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                            "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                            "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" + "<p>Scrolling Test</p>" +
                            "Test";

        return content;
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
